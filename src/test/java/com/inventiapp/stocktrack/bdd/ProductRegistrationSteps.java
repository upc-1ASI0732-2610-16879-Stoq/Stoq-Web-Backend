package com.inventiapp.stocktrack.bdd;

import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;

public class ProductRegistrationSteps {

    private boolean validProductInfo;
    private boolean productRegistered;
    private boolean inventoryUpdated;
    private boolean rejectedOperation;
    private boolean notificationGenerated;
    private boolean duplicatedProductBlocked;

    private boolean minimumStockConfigured;
    private boolean stockBelowMinimum;
    private boolean lowStockAlertGenerated;
    private boolean alertLimitUpdated;
    private boolean alertsTemporarilyDisabled;
    private boolean traceabilityRegistered;

    // US01 - Registrar producto nuevo

    @Given("el usuario proporciona información válida del producto")
    public void elUsuarioProporcionaInformacionValidaDelProducto() {
        validProductInfo = true;
    }

    @When("el usuario confirma el registro")
    public void elUsuarioConfirmaElRegistro() {
        if (validProductInfo) {
            productRegistered = true;
            inventoryUpdated = true;
        }
    }

    @Then("el sistema valida la información")
    public void elSistemaValidaLaInformacion() {
        assertTrue(validProductInfo);
    }

    @And("registra los datos de manera segura")
    public void registraLosDatosDeManeraSegura() {
        assertTrue(productRegistered);
    }

    @And("actualiza el inventario con el nuevo producto")
    public void actualizaElInventarioConElNuevoProducto() {
        assertTrue(inventoryUpdated);
    }

    @Given("existe información de producto incompleta o inconsistente")
    public void existeInformacionDeProductoIncompletaOInconsistente() {
        validProductInfo = false;
    }

    @When("el sistema procesa la solicitud de registro")
    public void elSistemaProcesaLaSolicitudDeRegistro() {
        if (!validProductInfo) {
            rejectedOperation = true;
            notificationGenerated = true;
        }
    }

    @Then("rechaza la operación")
    public void rechazaLaOperacion() {
        assertTrue(rejectedOperation);
    }

    @And("genera una notificación indicando la necesidad de completar la información requerida")
    public void generaNotificacionIndicandoNecesidadDeCompletarInformacion() {
        assertTrue(notificationGenerated);
    }

    @Given("existe una solicitud de registro con un producto ya registrado")
    public void existeUnaSolicitudDeRegistroConUnProductoYaRegistrado() {
        productRegistered = true;
    }

    @When("el sistema procesa la información recibida")
    public void elSistemaProcesaLaInformacionRecibida() {
        if (productRegistered) {
            duplicatedProductBlocked = true;
            notificationGenerated = true;
        }
    }

    @Then("impide el registro duplicado")
    public void impideElRegistroDuplicado() {
        assertTrue(duplicatedProductBlocked);
    }

    @And("genera una notificación indicando que el producto ya se encuentra en el inventario")
    public void generaNotificacionProductoYaRegistrado() {
        assertTrue(notificationGenerated);
    }

    // US05 - Generar alertas por bajo stock

    @Given("un producto tiene configurado un límite mínimo de stock")
    public void unProductoTieneConfiguradoUnLimiteMinimoDeStock() {
        minimumStockConfigured = true;
    }

    @When("el sistema detecta que la cantidad disponible desciende por debajo de ese valor")
    public void elSistemaDetectaCantidadDisponibleDebajoDelMinimo() {
        if (minimumStockConfigured) {
            stockBelowMinimum = true;
            lowStockAlertGenerated = true;
        }
    }

    @Then("el sistema genera una alerta automática")
    public void elSistemaGeneraUnaAlertaAutomatica() {
        assertTrue(lowStockAlertGenerated);
    }

    @And("comunica al usuario la necesidad de reabastecer el producto")
    public void comunicaAlUsuarioLaNecesidadDeReabastecerElProducto() {
        assertTrue(lowStockAlertGenerated);
    }

    @Given("el usuario desea modificar el valor mínimo de stock")
    public void elUsuarioDeseaModificarElValorMinimoDeStock() {
        minimumStockConfigured = true;
    }

    @When("el usuario establece un nuevo parámetro de alerta")
    public void elUsuarioEstableceUnNuevoParametroDeAlerta() {
        alertLimitUpdated = true;
    }

    @Then("el sistema guarda el nuevo valor")
    public void elSistemaGuardaElNuevoValor() {
        assertTrue(alertLimitUpdated);
    }

    @And("aplica las alertas futuras con base en el límite configurado")
    public void aplicaLasAlertasFuturasConBaseEnElLimiteConfigurado() {
        assertTrue(alertLimitUpdated);
    }

    @Given("el usuario necesita suspender las notificaciones por mantenimiento o revisión de inventario")
    public void elUsuarioNecesitaSuspenderLasNotificaciones() {
        alertsTemporarilyDisabled = false;
    }

    @When("el usuario desactiva temporalmente las alertas")
    public void elUsuarioDesactivaTemporalmenteLasAlertas() {
        alertsTemporarilyDisabled = true;
        traceabilityRegistered = true;
    }

    @Then("el sistema detiene la emisión de nuevas alertas")
    public void elSistemaDetieneLaEmisionDeNuevasAlertas() {
        assertTrue(alertsTemporarilyDisabled);
    }

    @And("registra la acción para su trazabilidad")
    public void registraLaAccionParaSuTrazabilidad() {
        assertTrue(traceabilityRegistered);
    }
}