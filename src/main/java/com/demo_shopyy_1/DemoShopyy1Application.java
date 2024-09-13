package com.demo_shopyy_1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com")
public class DemoShopyy1Application {

    public static void main(String[] args) {
        SpringApplication.run(DemoShopyy1Application.class, args);
//
//        // goc vuong duoi trai
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j <= i; j++) {
//                System.out.print("* ");
//            }
//            System.out.println();
//        }
//        System.out.println();
//
//        // goc vuong tren trai
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < 4 - i; j++) {
//                System.out.print("* ");
//            }
//            System.out.println();
//        }
//        System.out.println();
//
//        //goc vuong duoi phai
//        // i = row, j = column
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < 4; j++) {
//                if (j >= 3 - i) {
//                    System.out.print("* ");
//                } else {
//                    System.out.print("  ");
//                }
//            }
//            System.out.println();
//        }
//        System.out.println();
//
//        //goc vuong tren phai
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < 4; j++) {
//                if (j >= i) {
//                    System.out.print("* ");
//                } else {
//                    System.out.print("  ");
//                }
//            }
//            System.out.println();
//        }
    }
}