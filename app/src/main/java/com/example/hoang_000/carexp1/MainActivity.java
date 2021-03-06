package com.example.hoang_000.carexp1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.renderscript.Script;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoang_000.carexp1.Model2.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.nio.channels.CancelledKeyException;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    Button btnSignIn,btnRegister;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    RelativeLayout rootLayout;
    CheckBox ckbRemmeber;
    private final static int PEMISSION=1000;
    public static final String user_field="usr";
    public static final String pwd_field="pwd";
    private boolean exit = false;


    TextView txtForgotPassword;
  //  public static User currentUser;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_main);
        //init paper
        Paper.init(this);
        // init firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        //init view
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
        txtForgotPassword = (TextView) findViewById(R.id.txt_forgot_password);
        ckbRemmeber = (CheckBox) findViewById(R.id.ckbRemember);
        txtForgotPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showDialogForgotPassword();
                return false;
            }
        });
        //event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDialog();
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });
        //auto login

            String user = Paper.book().read(user_field);
            String pwd = Paper.book().read(pwd_field);
           if (user != null && pwd != null) {
                if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd)) {
                    autoLogin(user, pwd);

                }
            }
    }

    /**
     * Tu dong login moi khi phan mem duoc bat, hoac resume
     * @param user
     * @param pwd
     */
    private void autoLogin(String user, String pwd) {

        final AlertDialog waitingDialog = new SpotsDialog(MainActivity.this);
        waitingDialog.show();
        //login
        auth.signInWithEmailAndPassword(user,pwd)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override

                    public void onSuccess(AuthResult authResult) {
                        waitingDialog.dismiss();
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //save value

                                        Common.currentUser=dataSnapshot.getValue(User.class);
                                        waitingDialog.dismiss();
                                        startActivity(new Intent(MainActivity.this, UserHome.class));

                                        finish();



                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                waitingDialog.dismiss();
                Snackbar.make(rootLayout, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT)
                        .show();
                btnSignIn.setEnabled(true);
            }
        });



    }

    /**
     * chức năng reset lại mật khẩu khi người dùng quên mật khẩu
     * người dùng sẽ nhập email của mình rồi hệ thống sẽ gửi mã xác nhận
     */
    private void showDialogForgotPassword() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("QUÊN MẬT KHẨU");
        alertDialog.setMessage("Vui lòng nhập email của bạn để chúng tôi gửi mã xác nhận");

        LayoutInflater inflater=LayoutInflater.from(MainActivity.this);
        View forgot_pwd_layout= inflater.inflate(R.layout.layout_forgot_password,null);
        final MaterialEditText edtEmail=(MaterialEditText)forgot_pwd_layout.findViewById(R.id.edtEmail);
        alertDialog.setView(forgot_pwd_layout);

        //set button
        alertDialog.setPositiveButton("Tiếp tục", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
             final AlertDialog waitingDialog= new SpotsDialog(MainActivity.this);
             waitingDialog.show();

             auth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                         @Override
                         public void onComplete(@NonNull Task<Void> task) {
                             dialogInterface.dismiss();
                             waitingDialog.dismiss();
                             Snackbar.make(rootLayout,"reset password link has been sent",Snackbar.LENGTH_LONG)
                                     .show();
                         }
                     }).addOnFailureListener(new OnFailureListener() {
                 @Override
                 public void onFailure(@NonNull Exception e) {
                     dialogInterface.dismiss();
                     waitingDialog.dismiss();
                     Snackbar.make(rootLayout,""+e.getMessage(),Snackbar.LENGTH_LONG)
                             .show();
                 }
             });
            }
        });
        alertDialog.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    /**
     * chức năng đăng nhập vào ứng dụng
     * lưu ý mật khẩu không nhỏ hơn 6 ký tự
     */
    private void showLoginDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Chào mừng đến CAREXP");
        dialog.setMessage("Vui lòng nhập đầy đủ thông tin để đăng nhập");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login,null);
        final MaterialEditText edtEmail= login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword= login_layout.findViewById(R.id.edtPassword);


        dialog.setView(login_layout);
        //set button
        dialog.setPositiveButton("Đăng nhập", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                        //check validation
                        if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                            Snackbar.make(rootLayout, "Vui lòng nhập email", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                            Snackbar.make(rootLayout, "Vui lòng nhập mật khẩu", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        if (edtPassword.getText().toString().length() < 6) {
                            Snackbar.make(rootLayout, "Mật khẩu quá ngắn", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        final AlertDialog waitingDialog = new SpotsDialog(MainActivity.this);
                        waitingDialog.show();
                        //login
                        auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override

                                    public void onSuccess(AuthResult authResult) {
                                        waitingDialog.dismiss();
                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        //save value

                                                            Paper.book().write(user_field, edtEmail.getText().toString());
                                                            Paper.book().write(pwd_field, edtPassword.getText().toString());

                                                         Common.currentUser=dataSnapshot.getValue(User.class);
                                                        waitingDialog.dismiss();
                                                       // startActivity(new Intent(MainActivity.this, UserHome.class));
                                                        Intent intent = new Intent(MainActivity.this, UserHome.class);
                                                        startActivity(intent);
                                                        finish();



                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });



                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Snackbar.make(rootLayout, "Lỗi " + e.getMessage(), Snackbar.LENGTH_SHORT)
                                        .show();
                                btnSignIn.setEnabled(true);
                            }
                        });
                    }
                });


        dialog.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
             dialogInterface.dismiss();
            }
        });


        dialog.show();
    }

    /**
     * chức năng đăng ký tài khoản
     * người dùng nhập đầy đủ các thông tin cá nhân
     * lưu lại dữ liệu trên firebase
     */
    private void showRegisterDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Đăng ký tài khoản ");
        dialog.setMessage("Vui lòng điền đầy đủ thông tin về bạn, các đơn vị bạn cho phép mới được nhìn thấy");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register,null);
        final MaterialEditText edtEmail= register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword= register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName= register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone= register_layout.findViewById(R.id.edtPhone);

        dialog.setView(register_layout);
        //set button
        dialog.setPositiveButton("Đăng ký", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();


                //check validation
                if(TextUtils.isEmpty(edtEmail.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please enter email address",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(edtPassword.getText().toString().length()<6)
                {
                    Snackbar.make(rootLayout,"Password too short ",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edtName.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please enter your name",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edtPhone.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please enter phone",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                //register new user
                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                sendEmailVerification();
                              //Save to db
                                User user = new User();
                                user.setEmail(edtEmail.getText().toString());
                                user.setPassword(edtPassword.getText().toString());
                                user.setName(edtName.getText().toString());
                                user.setPhone(edtPhone.getText().toString());

                                //use email key
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                               // Snackbar.make(rootLayout,"Register success ",Snackbar.LENGTH_SHORT).show();
                                                return;
                                            }
                                        })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Snackbar.make(rootLayout,"Failed "+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                            return;
                                        }
                                    });
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout,"Failed ",Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
        });
     dialog.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
         }
     });
   dialog.show();
    }

    private void sendEmailVerification() {
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(MainActivity.this, "Vui lòng xác nhận đường link ở Email của bạn", Toast.LENGTH_LONG).show();
                       // FirebaseAuth.getInstance().signOut();
                    }
                }
            });
        }
    }


    /**
     * chức năng nhấn nút back 2 lần để thoát ứng dụng
     */
    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Nhấn nút trở về một lấn nữa để thoát.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3000);

        }
    }
}
