package server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerController {

    private ServerSocket serverSocket = null;
    private ExecutorService threadPool = null;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private JFrame frame;
    private JTextArea logArea;
    private JButton startBtn;
    private JButton stopBtn;

    // 可配置的端口与线程池大小
    private final int port = 12345;
    private final int poolSize = 10;


    // 创建并显示 GUI（实例方法）
    private void createFrame() {
        frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        content.setPreferredSize(new Dimension(800, 600));

        JLabel label = new JLabel("这是一个Server!", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(22f));
        content.add(label, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);
        content.add(scroll, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        startBtn = new JButton("Start");
        stopBtn = new JButton("Stop");
        stopBtn.setEnabled(false);
        controls.add(startBtn);
        controls.add(stopBtn);
        content.add(controls, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> startServer());
        stopBtn.addActionListener(e -> stopServer());

        frame.setContentPane(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // 在窗口关闭时优雅停服
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopServer();
            }
        });
    }

    // 启动服务器：在后台线程创建 ServerSocket 并运行 accept loop
    private void startServer() {
        if (running.get()) {
            appendLog("Server already running.");
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newFixedThreadPool(poolSize);
            running.set(true);
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            appendLog("Server started on port " + port);

            // accept loop 放到单独线程，避免阻塞 EDT
            Thread acceptThread = new Thread(() -> {
                try {
                    while (running.get()) {
                        Socket client = serverSocket.accept(); // 阻塞
                        appendLog("Accepted: " + client.getRemoteSocketAddress());
                        threadPool.submit(new ClientHandler(client));
                    }
                } catch (SocketException se) {
                    // 发生在 serverSocket.close() 时，安全忽略
                    appendLog("Server socket closed.");
                } catch (IOException ioe) {
                    appendLog("Accept error: " + ioe.getMessage());
                } finally {
                    stopServer(); // 确保资源释放
                }
            }, "Accept-Thread");
            acceptThread.start();

        } catch (IOException e) {
            appendLog("Failed to start server: " + e.getMessage());
            cleanupResources();
        }
    }

    // 停止服务器并关闭线程池
    private void stopServer() {
        if (!running.get()) return;
        running.set(false);
        appendLog("Stopping server...");
        cleanupResources();

        // 更新 GUI 按钮（回到 EDT）
        SwingUtilities.invokeLater(() -> {
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        });
    }

    // 关闭 socket 和线程池
    private void cleanupResources() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
        if (threadPool != null) {
            threadPool.shutdownNow();
            threadPool = null;
        }
        serverSocket = null;
    }

    // 把日志追加到 JTextArea（线程安全）
    private void appendLog(String s) {
        String message = "[" + java.time.LocalTime.now().withNano(0) + "] " + s + "\n";
        SwingUtilities.invokeLater(() -> {
            logArea.append(message);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    // 简单的客户端处理器：回显并记录
    private class ClientHandler implements Runnable {
        private final Socket socket;
        ClientHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try (Socket s = socket;
                 BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                 PrintWriter writer = new PrintWriter(s.getOutputStream(), true)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    appendLog("From " + s.getRemoteSocketAddress() + ": " + line);
                    writer.println("Echo: " + line); // 简单回显
                }
            } catch (IOException e) {
                appendLog("Client handler error: " + e.getMessage());
            } finally {
                appendLog("Client disconnected: " + socket.getRemoteSocketAddress());
            }
        }
    }

    // main：创建实例并在 EDT 上创建 GUI
    public static void main(String[] args) {
        // 如果需要连接数据库，建议在这里 new 并传入 controller（或由 handler 创建）
        // ConnectToDatabase db = new ConnectToDatabase(); // 如需

        SwingUtilities.invokeLater(() -> {
            ServerController controller = new ServerController();
            controller.createFrame();
        });
    }
}
