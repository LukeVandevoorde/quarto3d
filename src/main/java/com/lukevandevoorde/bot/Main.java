package com.lukevandevoorde.bot;

public class Main {
    public static void main(String[] args) {
        
        
    }

    public static void testIterTimes() {
        
    }

    public static void testMoveTimes() {
        long totalTime = 0;
        byte[] pieces = new byte[16];
        for (int i = 0; i < 15; i++) {
            pieces[i] = QB.piece((i/8)%2==0, (i/4)%2==0, (i/2)%2==0, i%2==0);
        }

        int numTimes = 5;
        int numBoards = 1000000;

        for (int it = 0; it < numTimes; it++) {
            QB[] qbs = new QB[numBoards];

            for (int i = 0; i < qbs.length; i++) {
                qbs[i] = new QB();
            }

            long start = System.nanoTime();
            for (int i = 0; i < qbs.length; i++) {
                for (int piece_i = 0; piece_i < 16; piece_i++) {
                    qbs[i].move(pieces[piece_i], piece_i/4, piece_i%4);
                }
            }
            totalTime += System.nanoTime() - start;
        }
        
        long avg = totalTime / numTimes;
        System.out.println("QB");
        System.out.println("Average time: " + avg);
        System.out.println("As seconds: " + (float)avg / (Math.pow(10, 9)));
        
        totalTime = 0;

        for (int it = 0; it < numTimes; it++) {
            QB_Arr[] qbs = new QB_Arr[numBoards];

            for (int i = 0; i < qbs.length; i++) {
                qbs[i] = new QB_Arr();
            }

            long start = System.nanoTime();
            for (int i = 0; i < qbs.length; i++) {
                for (int piece_i = 0; piece_i < 16; piece_i++) {
                    qbs[i].move(pieces[piece_i], piece_i/4, piece_i%4);
                }
            }
            totalTime += System.nanoTime() - start;
        }
        
        avg = totalTime / numTimes;
        System.out.println("QB_Arr");
        System.out.println("Average time: " + avg);
        System.out.println("As seconds: " + (float)avg / (Math.pow(10, 9)));
    }
}
