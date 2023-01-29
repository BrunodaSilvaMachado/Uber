package com.cursoandroid.uber.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.cursoandroid.uber.service.DatabaseService;
import com.google.firebase.database.Exclude;

public class Usuario implements DatabaseService.ModelId, Parcelable {
    String id;
    String nome;
    String senha;
    String email;
    String foto;
    String tipo;

    private String latitude;
    private String longitude;

    public Usuario(){}

    public Usuario(String nome, String email, String senha, String tipo) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
        this.latitude = "0";
        this.longitude="0";
    }

    protected Usuario(Parcel in) {
        id = in.readString();
        nome = in.readString();
        senha = in.readString();
        email = in.readString();
        foto = in.readString();
        tipo = in.readString();
        latitude = in.readString();
        longitude = in.readString();
    }

    public static final Creator<Usuario> CREATOR = new Creator<Usuario>() {
        @Override
        public Usuario createFromParcel(Parcel in) {
            return new Usuario(in);
        }

        @Override
        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Exclude
    @Override
    public int describeContents() {
        return 0;
    }

    @Exclude
    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(id);
        parcel.writeString(nome);
        parcel.writeString(senha);
        parcel.writeString(email);
        parcel.writeString(foto);
        parcel.writeString(tipo);
        parcel.writeString(latitude);
        parcel.writeString(longitude);
    }
}
