Feature: Registro de productos

  Para mantener un control actualizado y confiable de las existencias desde su ingreso
  Como Usuario de inventario
  Quiero registrar un nuevo producto en mi inventario

  Scenario: Registro exitoso de producto
    Given el usuario proporciona información válida del producto
    When el usuario confirma el registro
    Then el sistema valida la información
    And registra los datos de manera segura
    And actualiza el inventario con el nuevo producto

  Scenario: Información incompleta o inconsistente
    Given existe información de producto incompleta o inconsistente
    When el sistema procesa la solicitud de registro
    Then rechaza la operación
    And genera una notificación indicando la necesidad de completar la información requerida

  Scenario: Producto duplicado
    Given existe una solicitud de registro con un producto ya registrado
    When el sistema procesa la información recibida
    Then impide el registro duplicado
    And genera una notificación indicando que el producto ya se encuentra en el inventario