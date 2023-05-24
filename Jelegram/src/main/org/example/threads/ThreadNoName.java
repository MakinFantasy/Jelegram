package main.org.example.threads;

public class ThreadNoName {
    public static void main(String[] args) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("hi");
            }
        });
        thread.start();

        Thread thread1 = new Thread() {
            @Override
            public void run() {
                System.out.println("hello");
            }
        };
        thread1.start();
    }
}