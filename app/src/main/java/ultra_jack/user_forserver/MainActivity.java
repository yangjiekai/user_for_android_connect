package ultra_jack.user_forserver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    public static Handler mHandler=new Handler();
    //以下定義為mHandler成員的
    TextView TextView01;//對話框
    EditText EditText00;//IP輸入框
    EditText EditText10;//port輸入框
    EditText EditText01;//使用者名稱輸入框
    EditText EditText02;//訊息輸入框
    String receivedString; //接收串流
    Socket clientSocket; // 客戶端socket
    InetAddress serverIp; //司服器IP
    int serverPort;//司服器port
    //­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­­DropboxAPI
  //  private DropboxAPI<AndroidAuthSession> mDBApi;
    //本class中私有的mDBApi成員(下面final為不可改寫)
    final static String APP_KEY = "rhhcb11ttp050e9";
    final static String APP_SECRET = "faofqankjo1hp9k";
  //  final static AccessType ACCESS_TYPE = AccessType.APP_FOLDER; SharedPreferences prefs; //共享偏好物件導向定義為>> "prefs";


    SharedPreferences prefs; //共享偏好物件導向定義為>> "prefs";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView01 = (TextView) findViewById(R.id.TextView01);// ­­­­­­­­­­­­­­­­­TextView01	對話框
        EditText00 = (EditText) findViewById(R.id.editText00);// ­­­­­­­­­­­­­­­­­EditText00 IP輸入框
        EditText10 = (EditText) findViewById(R.id.editText10);// ­­­­­­­­­­­­­­­­­EditText10 port輸入框
        EditText01 = (EditText) findViewById(R.id.EditText01);// ­­­­­­­­­­­­­­­­­EditText01	使用者名稱輸入框
        EditText02 = (EditText) findViewById(R.id.EditText02);// ­­­­­­­­­­­­­­­­­EditText02	訊息輸入框
        Button button0 = (Button) findViewById(R.id.button00);// ­­­­­­­­­­­­­­­­­button0	連線按鈕
        Button button1 = (Button) findViewById(R.id.Button01);// ­­­­­­­­­­­­­­­­­button1	送出信息的按鈕
        Button button2 = (Button) findViewById(R.id.button_restart);//­­­­­­­­­­­­button2	重新啟動客戶端按鈕
        Button link = (Button) findViewById(R.id.button_Link2DB);//­­­­­­­­­­­­­­­link	登入Dropbox按鈕
        Button upload = (Button) findViewById(R.id.button2_Upload2DB);//­­­­­­­­­­upload	上傳Dropbox按鈕
        Button download = (Button) findViewById(R.id.button_DownloadDB);//­­­­­­­­download 還原Dropbox按鈕


        prefs= PreferenceManager.getDefaultSharedPreferences(this);
    download.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            File dir=new File (getFilesDir().getAbsolutePath());
            try {
                PrintWriter out=new PrintWriter(new FileWriter(dir+"/test.txt"));
                out.print(TextView01.getText().toString());
                out.close();
                File file=new File(getFilesDir().getAbsolutePath(),"/test.txt");
                FileInputStream in=new FileInputStream(file);



            } catch (IOException e) {
                e.printStackTrace();
            }



        }
    });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    File output = new File("/mnt/sdcard/test.txt");
                    OutputStream out = new FileOutputStream(output);
                    //mDBApi.getFile("/test.txt", null, out, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        button2.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                restart_p(); //restart_p()方法的程式碼在下方
            }
        });

        button0.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Thread t = new Thread(connect_readData);
                t.start();
            }
        });


        button1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (clientSocket.isConnected()) { //如果ClinentSocket連線完成
                    BufferedWriter bw; //宣告"BufferedWriter" 為 "bw"(為了簡化)
                    try {
                        bw = new BufferedWriter(new OutputStreamWriter(
                                clientSocket.getOutputStream()));
                        bw.write(EditText01.getText() + " 說 " //得到使用者輸入EditText01的名字後面加上"說" 例如 peter 說
                                + EditText02.getText() +"\n"); //得到使用者輸入的對話並空一行
                        bw.flush();//顯示出剛剛輸入的訊息 例如"peter 說 123"
                    } catch (IOException e) {
                    }
                    EditText02.setText(""); // 發送完訊息後自動清除輸入對話的地方
                }
            }
        });




    }

    private void restart_p() {

        AlarmManager alm= (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alm.set(AlarmManager.RTC,System.currentTimeMillis()+100,
                PendingIntent.getActivity(this,0,
                        new Intent(this,this.getClass()),0));
        android.os.Process.killProcess(android.os.Process.myPid());

    };

    private Runnable connect_readData = new Runnable() {


                @Override
                public void run() {

                    try {
                        String s = EditText00.getText().toString(); //讀取"EditText00"(IP輸入框裡的IP)並定義成"s"
                        if (s.equals(""))//如果沒寫IP就自動填入下面設定的IP
                            serverIp = InetAddress.getByName(" 192.168.0.111");
                        else//反之就讀取剛剛定義的"s"(使用者填入的)
                        serverIp = InetAddress.getByName(s);
                        String s2 = EditText10.getText().toString(); ///讀取"EditText10"(Port輸入框裡的port)並定義成"s2"
                        if (s2.equals(""))//如果沒寫Port就自動填入下面設定的Port
                            serverPort = 5050;
                        else//反之就讀取剛剛定義的"s2"(使用者填入的)
                        serverPort = Integer.parseInt(s2);
// EditText00.setText(serverIp.getHostAddress());
// This command here will result in error.
// Use runnable below instead
                        mHandler.post(writeBackServerIP); // 回寫ip和port到 "EditText框框"
                        clientSocket = new Socket(serverIp, serverPort);
                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                clientSocket.getInputStream()));
                        receivedString = "連線成功!";//連線成功就顯示文字
                        mHandler.post(updateUI); // post到"updateUI"(程式碼在下面)
// 讀取進來的資料 and update UI:
                        while (clientSocket.isConnected()) {
                            receivedString = br.readLine(); // 讀取近來的資料
                            if (receivedString != null)//如果有訊息就執行下面程式碼
                                mHandler.post(updateUI); // post到"updateUI"(程式碼在下面)
                        }
                    } catch (IOException e) {//反之就錯誤
                    }



                }
            };
            //並捲動ScrollView至訊息[updateUI]
            private Runnable updateUI = new Runnable() { //把下面程式碼導向為"updateUI"物件
                public void run() {
                    TextView01.append(receivedString + "\n");//顯示接收進來的訊息
                    ((ScrollView) findViewById(R.id.scrollView1)).post(new Runnable()
                    {
                        public void run()
                        {
                            ((ScrollView) findViewById(R.id.scrollView1)).fullScroll(View.FOCUS_DOWN);//把scrollView1自動拉到最下面(觀看最
                            //新訊息)
                        }
                    });
                }
            };
//  回寫ip和port到 "EditText框框" [writeBackServerIP]
            private Runnable writeBackServerIP = new Runnable() { //把下列的兩條程式碼導向成"writeBackServerIP"物件
                public void run() {
                    EditText00.setText(serverIp.getHostAddress());
                    EditText10.setText( Integer.toString(serverPort));
                }
            };



        }






