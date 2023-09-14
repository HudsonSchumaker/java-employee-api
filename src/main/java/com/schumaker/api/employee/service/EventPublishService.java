package com.schumaker.api.employee.service;

import com.schumaker.api.employee.view.dto.EmployeeDTO;
import com.schumaker.api.employee.view.dto.EmployeeEvent;
import com.schumaker.api.employee.model.enumeration.EmployeeEventType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventPublishService {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public EventPublishService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEvent(EmployeeEventType type, EmployeeDTO employeeDTO) {
        EmployeeEvent employeeEvent = new EmployeeEvent(type, employeeDTO);
        rabbitTemplate.convertAndSend("employee.queue", employeeEvent);
    }
}
