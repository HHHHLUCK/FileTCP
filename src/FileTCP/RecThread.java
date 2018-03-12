package FileTCP;

import javax.swing.*;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by han on 2017/6/25.
 */
public class RecThread implements Runnable {
    private static final int port = 6666;
    private static String path = "D:";
    private static ServerSocket server = null;
    private static Socket socket = null;

    public static void stopTask(){
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("close socket failed.");
            }

        }
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                System.err.println("close server failed.");
            }

        }
    }
    @Override
    public void run() {
        try {
            server = new ServerSocket(port);
            Thread th = new Thread(new Runnable() {
                public void run() {
                    try {
                        FileTransfer.status.setText("状态：等待链接...");
                        socket = server.accept();
                        System.out.println("有链接");
                        receiveFile(socket);
                    } catch (Exception e) {
                    }
                }

            });

            th.run(); //启动线程运行
        } catch (BindException e) {
            JOptionPane.showMessageDialog(FileTransfer.recFrame,
                    "端口：6666被占用，请勿同时打开多个接收界面");
            FileTransfer.recFrame.dispose();
            FileTransfer.mainFrame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }
    public static void receiveFile(Socket socket) {

        byte[] inputByte = null;
        int length = 0;
        DataInputStream dis = null;
        FileOutputStream fos = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            try {
                in = new BufferedReader(new  //用来获取文件名称和大小
                        InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());
                String name = in.readLine();
                Long count = 0L,fileSize = 0L;
                fileSize = Long.parseLong(name.substring(name.indexOf(' ') + 1));
                name = name.substring(0,name.indexOf(' '));
                System.out.println(name + "---" + fileSize);
                out.println("ok");
                out.flush();

                JOptionPane.showMessageDialog(FileTransfer.recFrame, "收到文件为：" + name +
                        "\n大小为：" + ((float) fileSize) / (1024 * 1024) + "MB");

                boolean flag = false;  //文件选择成功标志
                while (!flag){
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    chooser.setDialogTitle("请选择文件保存路径");
                    int returnVal = chooser.showOpenDialog(FileTransfer.recFrame);
                    if (returnVal == JFileChooser.APPROVE_OPTION){
                        System.out.println(chooser.getSelectedFile().getAbsolutePath());
                        path = chooser.getSelectedFile().getAbsolutePath();
                        flag = true;
                    }else {
                        JOptionPane.showMessageDialog(FileTransfer.recFrame,
                                "请选择正确的路径。");
                        //是否接收
                        int t = JOptionPane.showConfirmDialog(FileTransfer.recFrame,
                                "是否接收文件？","询问",JOptionPane.YES_NO_OPTION);
                        if (t != JOptionPane.YES_OPTION){
                            FileTransfer.recFrame.dispose();
                            FileTransfer.mainFrame.setVisible(true);
                            stopTask();
                            return;
                        }
                    }
                }

                System.out.println(path + "\\" + name);
                dis = new DataInputStream(socket.getInputStream());
                fos = new FileOutputStream(new File((path + "\\" + name)));
                inputByte = new byte[1024];

                System.out.println("开始接收数据...");
                FileTransfer.status.setText("状态：接收文件...");
                while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {
                    fos.write(inputByte, 0, length);
                    fos.flush();
                    count += length;
                    FileTransfer.progressBar.setValue((int) (((float) count / (float) fileSize) * 100));
                }
                System.out.println("完成接收");
                System.out.println("count: " + count + "    fileSize: " + fileSize);
                System.out.println(count.equals(fileSize));
                if (count.equals(fileSize))
                    FileTransfer.status.setText("状态：接收完成...");
                else {
                    JOptionPane.showMessageDialog(FileTransfer.recFrame,
                            "对方取消了发送。");
                    FileTransfer.status.setText("状态：对方取消了发送");
                    FileTransfer.recFrame.dispose();
                    FileTransfer.mainFrame.setVisible(true);
                    stopTask();
                }
            } finally {
                if (fos != null)
                    fos.close();
                if (dis != null)
                    dis.close();
                if (socket != null)
                    socket.close();
                if (FileTransfer.status.getText().equals("状态：接收完成..."))
                    FileTransfer.recOK();
            }
        } catch (Exception e) {

        }

    }
}
