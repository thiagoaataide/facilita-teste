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
