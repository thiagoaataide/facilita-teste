angular.module("ApuracaoChamadaApp", ["snk"])
    .controller("ApuracaoChamadaController", ["$scope", "ServiceProxy", "MessageUtils",
        function ($scope, ServiceProxy, MessageUtils) {
            var self = this;
            self.nuApuracao = "";
            self.resultado = "";
            self.listarAnexos = listarAnexos;

            function listarAnexos() {
                if (!self.nuApuracao) {
                    MessageUtils.showError("Informe o NUAPURACAO.");
                    return;
                }

                ServiceProxy.callService(
                    "facilita-apuracao-fatura-addon@ApuracaoDashboardSP.listarAnexos",
                    { request: { nuApuracao: String(self.nuApuracao) } }
                ).then(function (response) {
                    self.resultado = angular.toJson(response, true);
                    $scope.$applyAsync();
                }, function (error) {
                    self.resultado = angular.toJson(error, true);
                    $scope.$applyAsync();
                    MessageUtils.showError("A fachada recusou a chamada.");
                });
            }
        }]);
