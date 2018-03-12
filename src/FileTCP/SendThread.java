package FileTCP;

import javax.swing.*;
import java.io.*;
import java.net.*;

/**
 * Created by han on 2017/6/25.
 */
public class SendThread implements Runnable {
    private static final int port = 6666;
    private String path = "";
    private String strIp = "";
    byte[] bytes_ip = new byte[4];
    static Socket socket = null;
    public static boolean isCancel = false;

    public SendThread(String path, String strIp) {
        this.path = path;
        this.strIp = strIp;
//        System.out.println("---"+path);
//        System.out.println("---"+strIp);

    }

    public static void stopTask() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("close socket failed.");
            }

        }
    }

    @Override
    public void run() {
        String[] ips = strIp.split("[.]");
        if (ips.length != 4) {
            JOptionPane.showMessageDialog(FileTransfer.sendFrame, "输入的IP不对。");
            return;
        }
        for (int i = 0; i < 4; i++) {
            bytes_ip[i] = (byte) Integer.parseInt(ips[i]);
        }
        FileTransfer.status.setText("状态：正在连接...");

        int length = 0;
        byte[] sendBytes = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        FileInputStream fis = null;
        Long count = 0L;
        Long fileSize = 0L;
        try {
            try {
                socket = new Socket(strIp, port);
                socket.setSoTimeout(10000);
                FileTransfer.status.setText("状态：正在发送...");
                dos = new DataOutputStream(socket.getOutputStream());
                File file = new File(path);
//                System.out.println(file.length());
                fileSize = file.length();
                fis = new FileInputStream(file);
                dis = new DataInputStream(socket.getInputStream());
                sendBytes = new byte[1024];
//                System.out.println(file.getName());

                PrintWriter out = new PrintWriter(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new
                        InputStreamReader(socket.getInputStream()));
                out.println(file.getName() + " " + fileSize);
                out.flush();
                String str = in.readLine();
                System.out.println(str);

                System.out.println("开始发送数据");
                while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
                    dos.write(sendBytes, 0, length);
                    dos.flush();
                    count += length;
                    FileTransfer.progressBar.setValue((int) (((float) count / (float) fileSize) * 100));
                }
                FileTransfer.status.setText("状态：发送完成...");

            } catch (SocketTimeoutException e) {
                JOptionPane.showMessageDialog(FileTransfer.sendFrame,
                        "连接超时，请确认IP地址和对方接收状态。");
                FileTransfer.status.setText("状态：等待发送...");
            } catch (SocketException e) {
                System.out.println("取消发送");
                if (!isCancel && !count.equals(0L) ){
                    JOptionPane.showMessageDialog(FileTransfer.sendFrame,
                            "对方取消了接收。");
                    FileTransfer.status.setText("状态：对方取消了接收");
                }else if (count.equals(0L)){
                    JOptionPane.showMessageDialog(FileTransfer.sendFrame, "连接超时，请确认IP地址和对方接收状态。");
                    FileTransfer.status.setText("状态：等待发送...");
                }
                FileTransfer.sendFrame.dispose();
                FileTransfer.mainFrame.setVisible(true);
            } finally {
                if (dos != null)
                    dos.close();
                if (fis != null)
                    fis.close();
                if (socket != null)
                    socket.close();
                if (FileTransfer.status.getText().equals("状态：发送完成..."))
                    FileTransfer.sendOK();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
