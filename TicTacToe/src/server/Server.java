package server;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.Executors;

public class Server {

    private static File f;
    private static FileChannel channel;
    private static FileLock lock;
    
    public static void main(String[] args) throws Exception {
        //Your application tasks here..
        connectionNewServer();
        try (var listener = new ServerSocket(Integer.parseInt(args[0]))) {
            System.out.println("Tic Tac Toe Server is Running on address: 127.0.0.1, port: "+listener.getLocalPort()+"...");
            var pool = Executors.newFixedThreadPool(200);
            while (true) {
                if (!f.exists()) {
                    connectionNewServer();
                }
                Game game = new Game();
                pool.execute(game.new Player(listener.accept(), 'X'));
                pool.execute(game.new Player(listener.accept(), 'O'));
            }
        }
    }

    public static void connectionNewServer() {
        try {
            f = new File("123.lock");
            // Check if the lock exist
            if (f.exists()) {
                // if exist try to delete it
//                f.delete();
                System.out.println("123");
            }
            // Try to get the lock
            channel = new RandomAccessFile(f, "rw").getChannel();
            lock = channel.tryLock();
            if (lock == null) {
                // File is lock by other application
                channel.close();
                System.out.println("It's children server...");
            } else {
                System.out.println("It's header server...");
            }
            // Add shutdown hook to release lock when application shutdown
            ShutdownHook shutdownHook = new ShutdownHook();
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }catch(IOException e) {
                throw new RuntimeException("Could not start process.", e);
        }
    }
    public static void unlockFile() {
        // release and delete file lock
        try {
            if(lock != null) {
                System.out.println("Lock file has been unlock");
                lock.release();
                channel.close();
                f.delete();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    static class ShutdownHook extends Thread {

        public void run() {
            unlockFile();
        }
    }
}