package com.cursoandroid.uber.helper;

//TODO: converter em model e criar um serviço no firebase para recuperar os preços
public final class PrecoCorrida {
    public static final double VALOR_FIXO = 7;
    public static final double VALOR_POR_TEMPO = 10;
    public static final double VALOR_ESPECIAL = 0;
    public static final class VALOR_POR_KM {
        public static final double BASICO = 4;
        public static final double LUXO = 8;
    }

    public static double preco(double distancia, double tempo){
        return VALOR_FIXO + VALOR_ESPECIAL + distancia * VALOR_POR_KM.BASICO + tempo/VALOR_POR_TEMPO;
    }
}
