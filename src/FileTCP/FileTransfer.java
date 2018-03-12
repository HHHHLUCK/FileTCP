package FileTCP;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by han on 2017/6/25.
 */
public class FileTransfer extends JPanel implements ActionListener{
    public static int port = 6666;
    public static Font font = new Font("宋体", 0, 40);
    public static Font font_normal = new Font("宋体", 0, 24);
    public static Font font_small = new Font("宋体", 0, 18);
    public static JFrame mainFrame = new JFrame("文件传输");
    public static JButton recive = new JButton("接收文件");
    public static JButton send = new JButton("发送文件");
    public static JProgressBar progressBar = new JProgressBar(0, 100);
    public static JTextField status = new JTextField("状态: 等待连接...");
    public static JTextField sendIP = new JTextField("");
    public static JTextField sendFile = new JTextField("请选择文件");
    public static JFrame recFrame = new JFrame("接收文件");
    public static JFrame sendFrame = new JFrame("发送文件");
    public static Thread receiveThread;
    public static Thread sendThread;
    private static String path = "";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }

    public FileTransfer() {
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBackground(new Color(255, 255, 255));
        this.setLayout(null);
        recive.setFont(font);
        recive.setFocusPainted(false);
        recive.setBackground(new Color(245, 245, 245));
        recive.setBounds(15, 20, 370, 400);
        recive.addActionListener(this);
        recive.setActionCommand("receive");
        send.setFont(font);
        send.setFocusPainted(false);
        send.setBackground(new Color(245, 245, 245));
        send.setBounds(385, 20, 370, 400);
        send.addActionListener(this);
        send.setActionCommand("send");
        this.add(recive);
        this.add(send);

        status.setFont(font_normal);
        status.setBackground(new Color(255, 255, 255));
        status.setBorder(null);
        status.setEditable(false);

        sendIP.setFont(font_normal);
        sendIP.setBackground(new Color(255, 255, 255));

        sendFile.setFont(font_normal);
        sendFile.setBackground(new Color(255, 255, 255));
        sendFile.setBorder(null);
        sendFile.setEditable(false);

        progressBar.setFont(font_normal);
        progressBar.setStringPainted(true);
    }

    private static void createAndShowGUI(){
        mainFrame = new JFrame("文件传输");
        mainFrame.add(new FileTransfer());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(780, 480);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        mainFrame.setVisible(true);
        UIManager.put("OptionPane.font", font_small);
        UIManager.put("OptionPane.messageFont", font_small);
        UIManager.put("OptionPane.buttonFont", font_small);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("receive".equals(e.getActionCommand())){
            recFrame = new JFrame("接收文件");
            recFrame.add(drawRecPanel());
            receiveThread = new Thread(new RecThread());
            receiveThread.start();
            recFrame.setSize(780, 480);
            recFrame.setLocationRelativeTo(mainFrame);
            recFrame.setResizable(false);
            recFrame.setVisible(true);
            recFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setVisible(false);
        }else if ("send".equals(e.getActionCommand())){
            sendFrame = new JFrame("发送文件");
            sendFrame.add(drawSendPanel());
            sendFrame.setResizable(false);
            sendFrame.setSize(780, 480);
            sendFrame.setLocationRelativeTo(mainFrame);
            sendFrame.setVisible(true);
            sendFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setVisible(false);
        }else if ("cancel_rec".equals(e.getActionCommand())){
            recFrame.dispose();
            mainFrame.setVisible(true);
            RecThread.stopTask();
            receiveThread.interrupt();
            System.out.println("关闭recThread");
        }else if ("cancel_send".equals(e.getActionCommand())){
            SendThread.isCancel = true;
            sendFrame.dispose();
            mainFrame.setVisible(true);
            SendThread.stopTask();
            System.out.println("关闭sendThread");
        }else if ("sendBtn".equals(e.getActionCommand())){
            if (!status.getText().equals("状态：等待发送...")){
                JOptionPane.showMessageDialog(sendFrame, "文件正在发送中。");
            } else if (sendIP.getText().equals("") || path.equals("")) {
                JOptionPane.showMessageDialog(sendFrame,
                        "输入IP或选择文件。");
            }else {
                sendThread = new Thread(new SendThread(path, sendIP.getText()));
                sendThread.start();
            }

        }else if ("chooseFile".equals(e.getActionCommand())){
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("请选择文件");
            int returnVal = chooser.showOpenDialog(sendFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION){
                System.out.println(chooser.getSelectedFile().getAbsolutePath());
                path = chooser.getSelectedFile().getAbsolutePath();
                sendFile.setText(path.substring(path.lastIndexOf('\\') + 1));
            }
        }
    }

    public JPanel drawRecPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(255, 255, 255));
        progressBar.setValue(0);
        progressBar.setBounds(35, 130, 700, 30);
        panel.add(progressBar);
        status.setBounds(270, 210, 300, 30);
        panel.add(status);
        JButton cancel = new JButton("取消");
        cancel.setFont(font_normal);
        cancel.setBounds(330, 300, 100, 30);
        cancel.addActionListener(this);
        cancel.setActionCommand("cancel_rec");
        panel.add(cancel);
        return panel;
    }

    public JPanel drawSendPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(255, 255, 255));

        JTextField textField = new JTextField("IP：");
        textField.setFont(font_normal);
        textField.setBackground(new Color(255, 255, 255));
        textField.setBorder(null);
        textField.setEditable(false);
        textField.setBounds(35,50,50,30);
        panel.add(textField);

        sendIP.setBounds(85, 50, 300, 30);
        panel.add(sendIP);
        sendFile.setText("请选择文件");
        path = "";
        sendFile.setBounds(35, 130, 350, 30);
        panel.add(sendFile);
        JButton chooseFile = new JButton("选择文件");
        chooseFile.setFont(font_normal);
        chooseFile.setBounds(400, 130, 150, 30);
        chooseFile.setActionCommand("chooseFile");
        chooseFile.addActionListener(this);
        panel.add(chooseFile);

        progressBar.setValue(0);
        progressBar.setBounds(35, 210, 700, 30);
        panel.add(progressBar);
        status.setBounds(270, 290, 300, 30);
        status.setText("状态：等待发送...");
        panel.add(status);
        JButton cancel = new JButton("取消");
        cancel.setFont(font_normal);
        cancel.setBounds(400, 370, 100, 30);
        cancel.addActionListener(this);
        cancel.setActionCommand("cancel_send");
        panel.add(cancel);
        JButton sendBtn = new JButton("发送");
        sendBtn.setFont(font_normal);
        sendBtn.setBounds(270, 370, 100, 30);
        sendBtn.addActionListener(this);
        sendBtn.setActionCommand("sendBtn");
        panel.add(sendBtn);
        return panel;
    }

    public static void sendOK(){
        JOptionPane.showMessageDialog(sendFrame, "发送完成。");
        sendFrame.dispose();
        mainFrame.setVisible(true);
    }

    public static void recOK(){
        JOptionPane.showMessageDialog(recFrame, "接收完成。");
        recFrame.dispose();
        mainFrame.setVisible(true);
        RecThread.stopTask();
        receiveThread.interrupt();
    }
}

