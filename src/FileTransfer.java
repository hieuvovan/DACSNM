
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

    public class FileTransfer extends Frame {
    public static String hostAddress = "";
    public static int portNumber = 0, maxClients = 0;
    public static Vector sockets = null;
    public static FileTransfer tp;
    public static String fileName = "",path = "";
    public static int check = 0;
    public static Socket connection = null;
    public static ObjectOutputStream out = null;
    public static ObjectInputStream in = null;


        public static void main (String [] args) throws IOException {
        BufferedReader stdin = new BufferedReader
        (new InputStreamReader (System.in));
        System.out.println("1. Kết nối tới Server");
        System.out.println("2. Chạy với chức năng là Server");
        System.out.println();
        System.out.print("Hãy điền vào lựa chọn: ");
        System.out.flush();
        int choice = Integer.parseInt(stdin.readLine());


            if (choice == 1) {
            System.out.print("Nhập địa chỉ Server muốn kết nối: ");
            System.out.flush();
            hostAddress = stdin.readLine();
            System.out.print("Nhập cổng kết nối: ");
            System.out.flush();
            portNumber = Integer.parseInt(stdin.readLine());
        }


            if (choice == 2) {
            System.out.print("Chọn cổng để kết nối: ");
            System.out.flush();
            portNumber = Integer.parseInt(stdin.readLine());
            System.out.print("Chọn số Client tối đa có thể kết nối: ");
            System.out.flush();
            maxClients = Integer.parseInt(stdin.readLine());
        }
        tp = new FileTransfer(choice);
    }
    public Label l;
    public TextField tf;
    public Button browse;
    public Button send;
    public Button reset;


        public FileTransfer (int c) {
        setTitle("KofD");
        setSize(300 , 350);
        setLayout(null);
        addWindowListener(new WindowAdapter () { public void windowClosing (WindowEvent e) { System.exit(0); } } );
        l = new Label("File:");
        add(l);
        l.setBounds(15,87,50,20);
        tf = new TextField("");
        add(tf);
        tf.setBounds(13,114,200,20);
        browse = new Button("Chọn");
        browse.addActionListener(new buttonListener());
        add(browse);
        browse.setBounds(223,113,50,20);
        send = new Button("Gửi");
        send.addActionListener(new buttonListener());
        add(send);
        send.setBounds(64,168,50,20);
        reset = new Button("Xóa");
        reset.addActionListener(new buttonListener());
        add(reset);
        reset.setBounds(120,168,50,20);
        show();


            if (c == 1) {
            check = 10;


                try {
                connection = new Socket (hostAddress,portNumber);
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                int flag = 0;


                    while (true) {
                    Object recieved = in.readObject();


                        switch (flag) {
                        case 0:


                            if (recieved.equals("sot")) {
                            flag++;
                        }
                        break;
                        case 1:
                        fileName = (String) recieved;
                        int option = JOptionPane.showConfirmDialog(this,connection.getInetAddress().getHostName()+" đang gửi "+fileName+"!\nBạn muốn nhận?","Recieve Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);


                            if (option == JOptionPane.YES_OPTION) {
                            flag++;


                            } else {
                            flag = 0;
                        }
                        break;
                        case 2:
                        byte[] b = (byte[])recieved;
                        FileOutputStream ff = new FileOutputStream(fileName);
                        ff.write(b);
                        flag = 0;
                        JOptionPane.showMessageDialog(this,"Đã nhận file!","Confirmation",JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                    Thread.yield();
                }
            } catch (Exception e) {System.out.println(e);}
        }


            if (c == 2) {
            sockets = new Vector();
            check = 5;


                try {
                ServerSocket connect = new ServerSocket(portNumber,maxClients);


                    while (true) {
                    sockets.addElement(new ThreadedSocket(connect.accept()));
                    Thread.yield();
                }
            } catch (IOException ioe) {System.out.println(ioe);}
        }
    }


        public static String showDialog () {
        FileDialog fd = new FileDialog(new Frame(),"Select File...",FileDialog.LOAD);
        fd.show();
        return fd.getDirectory()+fd.getFile();
    }


        private class buttonListener implements ActionListener {


            public void actionPerformed (ActionEvent e) {
            byte[] array = null;


                if (e.getSource() == browse) {
                path = showDialog();
                tf.setText(path);
                int index = path.lastIndexOf("\\");
                fileName = path.substring(index+1);
            }


                if (e.getSource() == send) {
                        try {
                        FileInputStream f = new FileInputStream (path);
                        int size = f.available();
                        array = new byte[size];
                        f.read(array,0,size);


                            if (check == 5) {


                                for (int i=0;i<sockets.size();i++) {
                                ThreadedSocket temp = (ThreadedSocket)sockets.elementAt(i);
                                temp.out.writeObject("sot");
                                temp.out.flush();
                                temp.out.writeObject(fileName);
                                temp.out.flush();
                                temp.out.writeObject(array);
                                temp.out.flush();
                            }
                        }


                            if (check == 10) {
                            out.writeObject("sot");
                            out.flush();
                            out.writeObject(fileName);
                            out.flush();
                            out.writeObject(array);
                            out.flush();
                        }
                        JOptionPane.showMessageDialog(null,"Đã gửi file!","Confirmation",JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {}
                    //}
                }


                    if (e.getSource() == reset) {
                    tf.setText("");
                }
            }
        }
    }


    class ThreadedSocket extends Thread {
        public Socket socket;
        public ObjectInputStream in;
        public ObjectOutputStream out;


            public ThreadedSocket (Socket s) {
            socket = s;


                try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
            } catch (Exception e) {System.out.println(e);}
            start();
        }


            public void run () {


                try {
                int flag = 0;
                String fileName = "";


                    while (true) {
                    Object recieved = in.readObject();


                        switch (flag) {
                        case 0:


                            if (recieved.equals("sot")) {
                            flag++;
                        }
                        break;
                        case 1:
                        fileName = (String) recieved;
                        int option = JOptionPane.showConfirmDialog(null,socket.getInetAddress().getHostName()+" đang gửi "+fileName+"!\nBạn muốn nhận?","Recieve Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);


                            if (option == JOptionPane.YES_OPTION) {
                            flag++;


                            } else {
                            flag = 0;
                        }
                        break;
                        case 2:
                        byte[] b = (byte[])recieved;
                        FileOutputStream ff = new FileOutputStream(fileName);
                        ff.write(b);
                        flag = 0;
                        JOptionPane.showMessageDialog(null,"Đã nhận file!","Confirmation",JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                    Thread.yield();
                }
            } catch (Exception e) {System.out.println(e);}
        }
    }