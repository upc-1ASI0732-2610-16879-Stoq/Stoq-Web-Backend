Feature: Confirmar salida y descontar inventario

  Para mantener la exactitud del stock y asegurar el descuento correcto de los lotes
  Como Cajero
  Quiero confirmar una salida de productos en el sistema

  Scenario: Venta básica con consumo secuencial de lotes
    Given el sistema cuenta con un producto activo con stock distribuido en múltiples lotes
    When el usuario procesa una venta por una cantidad que supera el primer lote pero es cubierta por el segundo
    Then el sistema valida y procesa la solicitud exitosamente
    And agota por completo el inventario del primer lote
    And descuenta la cantidad restante del segundo lote
    And registra la venta de manera segura



