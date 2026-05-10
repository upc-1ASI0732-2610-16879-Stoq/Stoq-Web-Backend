Feature: Alertas por bajo stock

  Para realizar el reabastecimiento oportunamente y evitar quiebres de inventario
  Como Usuario de inventario
  Quiero recibir alertas cuando un producto se encuentra por debajo del stock mínimo

  Scenario: Activación automática de alerta por bajo stock
    Given un producto tiene configurado un límite mínimo de stock
    When el sistema detecta que la cantidad disponible desciende por debajo de ese valor
    Then el sistema genera una alerta automática
    And comunica al usuario la necesidad de reabastecer el producto

  Scenario: Personalización del límite de alerta
    Given el usuario desea modificar el valor mínimo de stock
    When el usuario establece un nuevo parámetro de alerta
    Then el sistema guarda el nuevo valor
    And aplica las alertas futuras con base en el límite configurado

  Scenario: Desactivación temporal de alertas
    Given el usuario necesita suspender las notificaciones por mantenimiento o revisión de inventario
    When el usuario desactiva temporalmente las alertas
    Then el sistema detiene la emisión de nuevas alertas
    And registra la acción para su trazabilidad