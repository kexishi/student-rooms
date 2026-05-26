package org.example.studyroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class StudyRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyRoomApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printApplicationUrl() {
        System.out.println("\n");
        System.out.println("自习室预约系统已启动！\n");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("\n");
    }

}
