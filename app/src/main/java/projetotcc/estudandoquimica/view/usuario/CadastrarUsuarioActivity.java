package projetotcc.estudandoquimica.view.usuario;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import projetotcc.estudandoquimica.MainActivity;
import projetotcc.estudandoquimica.R;
import projetotcc.estudandoquimica.UploadFiles;
import projetotcc.estudandoquimica.VerificarConexaoInternet;
import projetotcc.estudandoquimica.model.Usuario;
import projetotcc.estudandoquimica.view.compartilhado.CadastrarPublicacaoActivity;
import projetotcc.estudandoquimica.viewmodel.CadastrarUsuarioViewModel;
import projetotcc.estudandoquimica.databinding.ActivityUsuarioBinding;

public class CadastrarUsuarioActivity extends AppCompatActivity implements View.OnClickListener,
DatabaseReference.CompletionListener{

    private Bitmap bitmap;
    private boolean processVisible = false;

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private static String urlImagem = "";
    private static final int IMAGE_GALLERY_REQUEST = 1;
    private CadastrarUsuarioViewModel viewModel;
    private Uri uri;
    private ActivityUsuarioBinding usuarioBinding;
    private Activity activity;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private Usuario user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usuarioBinding = DataBindingUtil.setContentView(this, R.layout.activity_cadastrar_usuario);
       // usuarioBinding = ActivityUsuarioBinding.inflate(getLayoutInflater());
        activity = this;

        viewModel = ViewModelProviders.of(this).get(CadastrarUsuarioViewModel.class);
        usuarioBinding.setUser(viewModel);

        /*database.child("usuarios").child("002").child("nome").setValue("Iury");
        database.child("usuarios").setValue(new Usuario("iury", "email", "senha"));*/

        auth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                if( firebaseUser == null || user.getId() != null ){
                    return;
                }
                user.setNome(viewModel.nome.get());
                user.setEmail(viewModel.getUsuario().getEmail());
                user.setId( firebaseUser.getUid() );
                user.saveDB( CadastrarUsuarioActivity.this );
            }
        };
       /* auth.signOut();
        if(auth.getCurrentUser() != null){
            Log.i("logado", "usuario logado");
        }else{
            Log.i("nao logado", "usuario nao logado");
        }*/

        //Login

/*        auth.signInWithEmailAndPassword("iury_wemerson@hotmail.com", "iury123456")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.i("sucess", "Sucesso ao logar");
                        }else{
                            Log.i("Erro", "Erro ao logar");
                        }
                    }
                });*/

        //Cadastro

//        auth.createUserWithEmailAndPassword("iury_wemerson@hotmail.com", "iury123456")
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if(task.isSuccessful()){
//                            Log.i("sucess", "Sucesso ao cadastrar");
//                        }else{
//                            Log.i("Erro", "Erro ao cadastrar");
//                        }
//                    }
//                });


        final ImageView imageView = (ImageView)findViewById(R.id.foto);
        final ImageView add_foto = findViewById(R.id.add_foto);

        add_foto.setOnClickListener(this);
        imageView.setOnClickListener(this);

        user = new Usuario();
        user.setNome(usuarioBinding.nome.getText().toString());
        user.setSenha(usuarioBinding.senha.getText().toString());
        user.setEmail(usuarioBinding.email.getText().toString());

    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if( mAuthStateListener != null ){
            auth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        //aqui ele verifica se o usuário já deu a permissão....
        if (ActivityCompat.checkSelfPermission(CadastrarUsuarioActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //e caso ainda não tenha dado, ele solicita...
            ActivityCompat.requestPermissions(CadastrarUsuarioActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        }else{
            //ao executar novamente ele irá verificarTipoUsuario que já foi dado a permissão e irá executar a funcionalidade normalmente
            registerForContextMenu(v);
            openContextMenu(v);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);


        menu.setHeaderTitle("Selecione uma foto");
        menu.setHeaderIcon(R.drawable.ic_photo);

        MenuItem carregarImagem = menu.add(Menu.NONE,2,Menu.NONE,"Galeria");
        carregarImagem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                selecionarGaleria();
                return false;
            }
        });
    }

    public void selecionarGaleria(){
        Intent abrirGaleria = new Intent(Intent.ACTION_GET_CONTENT);
        abrirGaleria.setType("image/*");
        abrirGaleria.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(abrirGaleria,IMAGE_GALLERY_REQUEST);
    }

    public void selectImageClick(View view) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(
                            intent,
                            getString(R.string.title_1)),
                    IMAGE_GALLERY_REQUEST);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                    IMAGE_GALLERY_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == Activity.RESULT_OK){
            //new LoadImageTask(this).execute(data.getData());
            /*Uri selectedImage = data.getData();
            Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(selectedImage));
            Bitmap bitmapReduzido = Bitmap.createScaledBitmap(bitmap, 1080, 1000, true);

            showImage(bitmap);*/
            if(data != null) {
                carregarImagemGaleria(data);
            }

            //Toast.makeText(getApplicationContext(), selectedImage.toString(), Toast.LENGTH_SHORT).show();

        }
    }

    public void carregarImagemGaleria(Intent data){
        InputStream stream = null;

        try {
            if(bitmap != null){
                bitmap.recycle();
            }

            uri = data.getData();
            stream = getContentResolver().openInputStream(data.getData());
            bitmap = BitmapFactory.decodeStream(stream);

            final ImageView imageView = (ImageView)findViewById(R.id.foto);
            imageView.setImageBitmap(bitmap);

            exibirMsg("Imagem carregada com sucesso!"); //método criado logo abaixo, apenas para carregar uma mensagem de sucesso.
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void exibirMsg(String msg){
        View viewById = findViewById(R.id.telacadastro);
        Snackbar.make(viewById, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();


    }

    public void showImage(Bitmap bitmap) {
        ImageView imageView = (ImageView)findViewById(R.id.foto);
        if (imageView != null){
            imageView.setImageBitmap(bitmap);
        }
    }

    public void onCreateUserClicked(View view){

       if(validarCampos(viewModel.getUsuario())){


           if(!VerificarConexaoInternet.verificaConexao(this)){
               Snackbar snackbar = Snackbar
                       .make(usuarioBinding.telacadastro, "Sem acesso à internet", Snackbar.LENGTH_LONG);
               snackbar.show();
               return;
           }

           showProgress(true);

           auth.createUserWithEmailAndPassword(viewModel.getUsuario().getEmail(), viewModel.getUsuario().getSenha())
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        Log.i("TAG", "usuario cadastrado");
//                        if(bitmap != null){
//                            uploadImagem();
//////                            StorageReference ref = storageReference.child("imagens/perfils/" +
//////                                    viewModel.getUsuario().getNome() + "/" + auth.getCurrentUser().getUid());
////
////                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("usuarios/" + auth.getCurrentUser().getUid());
////                            UploadFiles uploadFiles = new UploadFiles(CadastrarUsuarioActivity.this);
////                            uploadFiles.upload(bitmap, "usuarios/" + auth.getCurrentUser().getUid() + "/", reference);
//                        }
                       // showProgress(true);
                       // saveUserInformation();

                    }else{

                        try{
                            throw task.getException();

                        }catch(FirebaseAuthUserCollisionException error) {

                            usuarioBinding.email.setError("Já existe um usuário com esse e-mail");

                        }catch (Exception e) {
                            Log.e("Erro", e.getMessage() + e.getCause());
                        }
                        showProgress(false);
                    }

                }
            }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
               @Override
               public void onSuccess(AuthResult authResult) {
                   if(bitmap != null){
                       uploadImagem();
                   }else {
                       showProgress(false);
                       Intent it = new Intent(CadastrarUsuarioActivity.this, ProfessorAlunoActivity.class);
                       it.putExtra("usuario", user);
                       startActivity(it);
                       overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                       finish();
                   }
               }
           });
       }


    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        processVisible = show;
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        usuarioBinding.formCadastro.setVisibility(show ? View.GONE : View.VISIBLE);
        usuarioBinding.formCadastro.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                usuarioBinding.formCadastro.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        usuarioBinding.cadastroProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        usuarioBinding.cadastroProgress.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                usuarioBinding.cadastroProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private boolean validarCampos(Usuario usuario){

        boolean valid = true;
        View focusView = null;

        if(usuario == null){
            return false;
        }

        if (TextUtils.isEmpty(usuario.getSenha())) {

            usuarioBinding.senha.setError(getString(R.string.error_field_required));
            valid = false;
            focusView = usuarioBinding.senha;
        }else if(!usuario.senhaValida(usuario.getSenha())){

            usuarioBinding.senha.setError(getString(R.string.error_invalid_password));
            valid = false;
            focusView = usuarioBinding.senha;
        }

        if (TextUtils.isEmpty(usuario.getEmail())) {

            usuarioBinding.email.setError(getString(R.string.error_field_required));
            focusView = usuarioBinding.email;
            valid = false;

        } else if (!usuario.emailValido(usuario.getEmail())) {

            usuarioBinding.email.setError(getString(R.string.error_invalid_email));
            focusView = usuarioBinding.email;
            valid = false;
        }

        if(TextUtils.isEmpty(usuario.getNome())){

            usuarioBinding.nome.setError(getString(R.string.error_field_required));
            valid = false;
            focusView = usuarioBinding.nome;
        }

        if (!valid) {

            focusView.requestFocus();
        }

        if(uri == null){

            valid = false;
            Toast.makeText(this, "Insira uma foto!", Toast.LENGTH_LONG).show();

        }

        return valid;
    }

    public void onCancelarCadastro(View v){

        finish();
       // uploadImageToFirebaseStorage();

       // uploadImagem();
    }

    private void uploadImagem(){
        FirebaseStorage storage;
        StorageReference storageReference;

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if(uri != null){
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("Salvando informações...");
//            progressDialog.show();
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progressDialog.setIndeterminate(false);
            showProgress(true);
            //UUID.randomUUID().toString()
            StorageReference ref = storageReference.child("imagens/perfils/" +
                    viewModel.getUsuario().getNome() + "/" + auth.getCurrentUser().getUid());

            UploadTask uploadTask = ref.putFile(uri);
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                   // progressDialog.setMessage("Enviando: "+(int)progress + "%");
                }
            }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {

                        Uri downloadUri = task.getResult();
                      //  user.setUrlFoto(downloadUri.toString());
                        saveUserInformation(downloadUri);

                        //progressDialog.dismiss();



                    } else {

                        showProgress(false);
                        Toast.makeText(CadastrarUsuarioActivity.this, "Falha no UPLOAD", Toast.LENGTH_SHORT).show();

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //progressDialog.dismiss();
                    showProgress(false);
                    Toast.makeText(CadastrarUsuarioActivity.this, "Falha: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void uploadImageToFirebaseStorage() {

        StorageReference profileImageRef =
                FirebaseStorage.getInstance().getReference("imagens/perfil/" + System.currentTimeMillis() + ".jpg");

        uri = Uri.parse("android.resource://projetotcc.estudandoquimica/drawable/iury");
        if (uri != null) {
            usuarioBinding.cadastroProgress.setVisibility(View.VISIBLE);
          /*profileImageRef.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return profileImageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if(task.isSuccessful()){
                        usuarioBinding.cadastroProgress.setVisibility(View.GONE);
                        urlImagem = task.getResult().toString();

                    }
                }
            });*/
            profileImageRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            usuarioBinding.cadastroProgress.setVisibility(View.GONE);
                            //urlImagem = profileImageRef.getDownloadUrl().toString();
                           // urlImagem = profileImageRef.getDownloadUrl().getResult().toString();
                           // urlImagem = "https://firebasestorage.googleapis.com/v0/b/projeto-tcc-appquimica.appspot.com/o/imagens%2Fperfil%2Fiury%2F1535400211833.jpg?alt=media&token=1c6463d2-5901-49d6-a174-509ad0a36eb2";


                            Log.i("TAG", urlImagem);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            usuarioBinding.cadastroProgress.setVisibility(View.GONE);
                            Toast.makeText(CadastrarUsuarioActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.i("TAG", "Deu ruim");
                        }
                    });

            //usuarioBinding.foto.setImageURI(uri);

            StorageReference result =
                    FirebaseStorage.getInstance().getReference("imagens/perfil/" + System.currentTimeMillis() + ".jpg");

           profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.i("URL", uri.toString());
                    //usuarioBinding.foto.setImageURI(uri);

                }
            });
        }


    }

    private void saveUserInformation(Uri pathImagem) {

        String displayName = viewModel.getUsuario().getNome();

        FirebaseUser user = auth.getCurrentUser();
       // String url = "usuarios/" + auth.getCurrentUser().getUid() + "imagens/url";


        if (user != null && pathImagem != null) {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(pathImagem)
                    .build();

//            Log.i("PATH", user.getPhotoUrl().toString() + user.getDisplayName());

            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(CadastrarUsuarioActivity.this, "Falha ao cadastrar perfil", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
//                            DatabaseReference ref = FirebaseDatabase.getInstance().
//                                    getReference("usuarios/" +  auth.getCurrentUser().getUid());
//
//                            ref.child("urlFoto").setValue(pathImagem.toString());
                            showProgress(false);
                            Intent it = new Intent(CadastrarUsuarioActivity.this, ProfessorAlunoActivity.class);

                            Usuario usuario = new Usuario();
                            usuario.setNome(user.getDisplayName());
                            usuario.setId(user.getUid());
                            usuario.setEmail(user.getEmail());
                            usuario.setUrlFoto(pathImagem.toString());

                            it.putExtra("usuario", usuario);
                            startActivity(it);
                            finish();
                            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                        }
                    });
        }
    }

    @Override
    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

        //Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {

        if(!processVisible){
            super.onBackPressed();
        }
    }
}


/*class LoadImageTask extends AsyncTask {

    WeakReference<CadastrarUsuarioActivity> mActivity;

    public LoadImageTask(CadastrarUsuarioActivity activity) {
        this.mActivity = new WeakReference(activity);
    }

    public CadastrarUsuarioActivity getActivity() {
        return mActivity.get();
    }


    protected Bitmap doInBackground(Uri... params) {
        if (getActivity() != null) {
            try {
                return BitmapFactory.decodeStream(
                        getActivity().getContentResolver().openInputStream(params[0]));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (getActivity() != null){
            getActivity().showImage(bitmap);
        }
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        return null;
    }
}*/


