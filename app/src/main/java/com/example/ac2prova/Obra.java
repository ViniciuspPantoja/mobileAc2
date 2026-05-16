package com.example.ac2prova;

public class Obra {
    private String id;
    private String nome;
    private boolean filmeOuSerie; // false = Filme, true = Série
    private String genero;
    private int anoLancamento;
    private double notaPessoal;
    private boolean seJaViuOuN;

    public Obra() {
    }

    public Obra(String id, String nome, boolean filmeOuSerie, String genero, int anoLancamento, double notaPessoal, boolean seJaViuOuN) {
        this.id = id;
        this.nome = nome;
        this.filmeOuSerie = filmeOuSerie;
        this.genero = genero;
        this.anoLancamento = anoLancamento;
        this.notaPessoal = notaPessoal;
        this.seJaViuOuN = seJaViuOuN;
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

    public boolean isFilmeOuSerie() {
        return filmeOuSerie;
    }

    public void setFilmeOuSerie(boolean filmeOuSerie) {
        this.filmeOuSerie = filmeOuSerie;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getAnoLancamento() {
        return anoLancamento;
    }

    public void setAnoLancamento(int anoLancamento) {
        this.anoLancamento = anoLancamento;
    }

    public double getNotaPessoal() {
        return notaPessoal;
    }

    public void setNotaPessoal(double notaPessoal) {
        this.notaPessoal = notaPessoal;
    }

    public boolean isSeJaViuOuN() {
        return seJaViuOuN;
    }

    public void setSeJaViuOuN(boolean seJaViuOuN) {
        this.seJaViuOuN = seJaViuOuN;
    }

    public String getTipoTexto() {
        return filmeOuSerie ? "Série" : "Filme";
    }

    public String getAssistidoTexto() {
        return seJaViuOuN ? "Já assistiu" : "Deseja assistir";
    }

    @Override
    public String toString() {
        return nome + " | " + getTipoTexto() + " | " + genero + " | " + anoLancamento + " | Nota: " + notaPessoal + " | " + getAssistidoTexto();
    }
}
