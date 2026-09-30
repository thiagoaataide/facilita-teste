angular.module("ApuracaoTrabalhoApp", ["snk"])
    .controller("ApuracaoTrabalhoController", ["$scope", "ServiceProxy",
        function ($scope, ServiceProxy) {
            var self = this;
            var fachada = "facilita-apuracao-fatura-addon@ApuracaoDashboardSP";
            self.itens = [];
            self.erro = "";
            self.detalhe = null;
            self.detalheTexto = "";
            self.anexos = null;
            self.anexoEscolhido = null;
            self.tipoAnexo = "";
            self.temArquivo = false;
            self.filtro = {
                mes: mesCorrente(),
                somentePendentes: true,
                possuiAnexo: false
            };
            self.gravando = false;
            self.selecionar = selecionar;
            self.listar = listar;
            self.confirmar = confirmar;
            self.solicitarNovaAuditoria = solicitarNovaAuditoria;
            self.escolherAnexo = escolherAnexo;
            self.marcarArquivo = marcarArquivo;
            self.verAnexo = verAnexo;
            self.enviarAnexo = enviarAnexo;

            listar();

            function listar() {
                self.detalhe = null;
                self.anexos = null;
                self.anexoEscolhido = null;
                atualizarGrade();
            }

            function confirmar() {
                gravar("confirmar", {});
            }

            function solicitarNovaAuditoria() {
                gravar("solicitarNovaAuditoria", {});
            }

            function gravar(metodo, extras) {
                if (!self.detalhe || self.gravando) {
                    return;
                }
                var request = {
                    nuApuracao: self.detalhe.nuApuracao,
                    version: self.detalhe.version,
                    idempotencyKey: novaChave(metodo)
                };
                angular.extend(request, extras);
                self.gravando = true;
                chamar(metodo, { request: request }).then(function (envelope) {
                    self.gravando = false;
                    if (!envelope) {
                        $scope.$applyAsync();
                        return;
                    }
                    self.detalhe = envelope.data || self.detalhe;
                    self.detalheTexto = angular.toJson(self.detalhe, true);
                    atualizarGrade();
                });
            }

            function novaChave(metodo) {
                return metodo + "-" + new Date().getTime() + "-"
                    + Math.random().toString(36).substring(2, 10);
            }

            function atualizarGrade() {
                chamar("listar", {
                    request: {
                        mesReferencia: textoMes(self.filtro.mes),
                        somentePendentes: self.filtro.somentePendentes === true,
                        possuiAnexo: self.filtro.possuiAnexo === true,
                        pagina: 0,
                        tamanhoPagina: 500
                    }
                }).then(function (envelope) {
                    if (!envelope) {
                        return;
                    }
                    var data = envelope.data || {};
                    self.itens = data.items || [];
                    $scope.$applyAsync();
                });
            }

            function selecionar(item) {
                if (!item || !item.nuApuracao) {
                    return;
                }
                self.detalhe = null;
                self.anexos = null;
                self.anexoEscolhido = null;
                chamar("listarDetalhe", {
                    request: { nuApuracao: item.nuApuracao }
                }).then(function (envelope) {
                    if (!envelope) {
                        return null;
                    }
                    self.detalhe = envelope.data || null;
                    self.detalheTexto = angular.toJson(self.detalhe, true);
                    return chamar("listarAnexos", {
                        request: { nuApuracao: item.nuApuracao }
                    });
                }).then(function (envelope) {
                    if (!envelope) {
                        return;
                    }
                    var data = envelope.data || {};
                    self.anexos = data.files || [];
                    self.anexoEscolhido = null;
                    $scope.$applyAsync();
                });
            }

            function escolherAnexo(arquivo) {
                if (!arquivo || !arquivo.identifier) {
                    return;
                }
                self.anexoEscolhido = arquivo.identifier;
            }

            function marcarArquivo(input) {
                self.temArquivo = !!(input && input.files && input.files.length > 0);
                $scope.$applyAsync();
            }

            function verAnexo() {
                if (!self.anexoEscolhido || !self.detalhe) {
                    return;
                }
                chamar("abrirAnexo", {
                    request: {
                        nuApuracao: self.detalhe.nuApuracao,
                        nuAttach: Number(self.anexoEscolhido)
                    }
                }).then(function (envelope) {
                    if (!envelope || !envelope.data || !envelope.data.url) {
                        return;
                    }
                    window.open(envelope.data.url, "_blank", "noopener");
                });
            }

            function enviarAnexo() {
                var input = document.getElementById("anexo-arquivo");
                var file = input && input.files ? input.files[0] : null;
                if (!self.detalhe || !file || !self.tipoAnexo || self.gravando) {
                    return;
                }
                var nu = self.detalhe.nuApuracao;
                var tipo = self.tipoAnexo;
                self.gravando = true;
                chamar("prepararAnexo", {
                    request: { nuApuracao: nu, nameAttach: file.name, tipo: tipo }
                }).then(function (envelope) {
                    if (!envelope) {
                        self.gravando = false;
                        return null;
                    }
                    var sessionKey = "ANEXO_SISTEMA_bhApuracao_" + nu;
                    var form = new FormData();
                    form.append("arquivo", file, file.name);
                    return fetch(window.location.origin + "/mge/sessionUpload.mge?sessionkey="
                            + encodeURIComponent(sessionKey) + "&fitem=S&salvar=S&useCache=N", {
                        method: "POST",
                        credentials: "same-origin",
                        body: form
                    });
                }).then(function (response) {
                    if (!response) {
                        return null;
                    }
                    if (!response.ok) {
                        self.gravando = false;
                        mostrarErro({
                            ok: false,
                            correlationId: "",
                            error: { code: "INTEGRATION", message: "O upload do arquivo foi recusado." }
                        });
                        return null;
                    }
                    return ServiceProxy.callService("AnexoSistemaSP.salvar", {
                        params: {
                            pkEntity: String(nu),
                            keySession: "ANEXO_SISTEMA_bhApuracao_" + nu,
                            nameEntity: "bhApuracao",
                            description: tipo,
                            keyAttach: "",
                            typeAcess: "ALL",
                            typeApres: "GLO",
                            nuAttach: "",
                            nameAttach: file.name,
                            fileSelect: 1,
                            oldFile: file.name
                        }
                    });
                }).then(function (result) {
                    if (!result) {
                        self.gravando = false;
                        return null;
                    }
                    var nuAttach = lerNuAttach(result);
                    if (!nuAttach) {
                        self.gravando = false;
                        mostrarErro({
                            ok: false,
                            correlationId: "",
                            error: { code: "INTEGRATION", message: "O anexo nao devolveu identificador." }
                        });
                        return null;
                    }
                    return chamar("concluirAnexo", {
                        request: { nuApuracao: nu, nuAttach: Number(nuAttach), tipo: tipo }
                    });
                }).then(function (envelope) {
                    self.gravando = false;
                    if (!envelope) {
                        return;
                    }
                    if (input) {
                        input.value = "";
                    }
                    self.temArquivo = false;
                    self.tipoAnexo = "";
                    return chamar("listarAnexos", { request: { nuApuracao: nu } });
                }).then(function (envelope) {
                    if (!envelope) {
                        return;
                    }
                    self.anexos = (envelope.data && envelope.data.files) || [];
                    self.anexoEscolhido = null;
                    $scope.$applyAsync();
                }).catch(function () {
                    self.gravando = false;
                    mostrarErro({
                        ok: false,
                        correlationId: "",
                        error: { code: "INTEGRATION", message: "Nao foi possivel enviar o anexo." }
                    });
                });
            }

            function lerNuAttach(result) {
                var body = result && result.responseBody ? result.responseBody : result;
                var chave = body && body.chave;
                if (chave && chave.valor) {
                    return chave.valor;
                }
                return null;
            }

            function chamar(metodo, payload) {
                self.erro = "";
                return ServiceProxy.callService(fachada + "." + metodo, payload)
                    .then(function (response) {
                        var envelope = lerEnvelope(response);
                        if (!envelope || envelope.ok !== true) {
                            mostrarErro(envelope);
                            return null;
                        }
                        return envelope;
                    }, function (error) {
                        mostrarErro(lerEnvelope(error) || erroDe(error));
                        return null;
                    });
            }

            function mostrarErro(envelope) {
                var error = envelope && envelope.error ? envelope.error : {};
                self.erro = "code: " + (error.code || "")
                    + "\nmessage: " + (error.message || "")
                    + "\ncorrelationId: " + ((envelope && envelope.correlationId) || "");
                $scope.$applyAsync();
            }

            function lerEnvelope(response) {
                if (!response) {
                    return null;
                }
                if (response.body && typeof response.body.ok === "boolean") {
                    return response.body;
                }
                if (response.responseBody && response.responseBody.body
                        && typeof response.responseBody.body.ok === "boolean") {
                    return response.responseBody.body;
                }
                if (response.responseBody && response.responseBody.error
                        && typeof response.responseBody.error.ok === "boolean") {
                    return response.responseBody.error;
                }
                if (typeof response.ok === "boolean") {
                    return response;
                }
                return null;
            }

            function erroDe(error) {
                return {
                    ok: false,
                    correlationId: "",
                    error: {
                        code: "",
                        message: angular.toJson(error)
                    }
                };
            }

            function mesCorrente() {
                var hoje = new Date();
                return new Date(hoje.getFullYear(), hoje.getMonth(), 1);
            }

            function textoMes(data) {
                if (typeof data === "string" && /^\d{4}-\d{2}$/.test(data)) {
                    return data;
                }
                if (!(data instanceof Date) || isNaN(data.getTime())) {
                    data = mesCorrente();
                }
                var mes = data.getMonth() + 1;
                return data.getFullYear() + "-" + (mes < 10 ? "0" + mes : String(mes));
            }
        }]);
