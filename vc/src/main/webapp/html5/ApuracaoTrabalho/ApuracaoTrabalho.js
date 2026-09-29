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
            self.selecionar = selecionar;

            listar();

            function listar() {
                chamar("listar", {
                    request: {
                        mesReferencia: mesCorrente(),
                        somentePendentes: true,
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
                    $scope.$applyAsync();
                });
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
                var mes = hoje.getMonth() + 1;
                var textoMes = mes < 10 ? "0" + mes : String(mes);
                return hoje.getFullYear() + "-" + textoMes;
            }
        }]);
