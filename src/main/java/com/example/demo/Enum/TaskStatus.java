package com.example.demo.Enum;

public enum TaskStatus {

    NEW,  //новая задача, еще не назначена сотруднику
    IN_PROGRESS,  //задача в работе
    CANCELLED,  //задача отменена
    ON_HOLD,  //задача на ожидании (например, когда ждем какую-то доп инфу)
    COMPLETED, //задача выполнена
    FAILED,  //задача не выполнена
    ARCHIVE //Архив
}
