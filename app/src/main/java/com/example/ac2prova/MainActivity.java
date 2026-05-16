package com.example.ac2prova;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText editNome, editAno, editNota, editBusca;
    private Spinner spinnerTipo, spinnerGenero, spinnerFiltroTipo;
    private CheckBox checkAssistiu, checkFiltroAssistidos;
    private Button btnSalvar, btnLimpar;
    private ListView listViewObras;

    private FirebaseFirestore db;

    private ArrayList<Obra> listaObras;
    private ArrayList<Obra> listaFiltrada;
    private ArrayAdapter<Obra> adapter;

    private String idSelecionado = null;

    private final String[] tipos = {"Selecione o tipo", "Filme", "Série"};
    private final String[] generos = {"Selecione o gênero", "Ação", "Comédia", "Drama", "Terror", "Ficção Científica"};
    private final String[] filtrosTipo = {"Todos os tipos", "Filme", "Série"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        listaObras = new ArrayList<>();
        listaFiltrada = new ArrayList<>();

        iniciarComponentes();
        configurarSpinners();
        configurarListView();
        configurarEventos();
        listarObras();
    }

    private void iniciarComponentes() {
        editNome = findViewById(R.id.editNome);
        editAno = findViewById(R.id.editAno);
        editNota = findViewById(R.id.editNota);
        editBusca = findViewById(R.id.editBusca);

        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerGenero = findViewById(R.id.spinnerGenero);
        spinnerFiltroTipo = findViewById(R.id.spinnerFiltroTipo);

        checkAssistiu = findViewById(R.id.checkAssistiu);
        checkFiltroAssistidos = findViewById(R.id.checkFiltroAssistidos);

        btnSalvar = findViewById(R.id.btnSalvar);
        btnLimpar = findViewById(R.id.btnLimpar);
        listViewObras = findViewById(R.id.listViewObras);
    }

    private void configurarSpinners() {
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);

        ArrayAdapter<String> adapterGenero = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, generos);
        adapterGenero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenero.setAdapter(adapterGenero);

        ArrayAdapter<String> adapterFiltro = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filtrosTipo);
        adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltroTipo.setAdapter(adapterFiltro);
    }

    private void configurarListView() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaFiltrada);
        listViewObras.setAdapter(adapter);
    }

    private void configurarEventos() {
        btnSalvar.setOnClickListener(v -> salvarObra());
        btnLimpar.setOnClickListener(v -> limparCampos());

        listViewObras.setOnItemClickListener((parent, view, position, id) -> {
            Obra obra = listaFiltrada.get(position);
            carregarParaEdicao(obra);
        });

        listViewObras.setOnItemLongClickListener((parent, view, position, id) -> {
            Obra obra = listaFiltrada.get(position);
            confirmarExclusao(obra);
            return true;
        });

        spinnerFiltroTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        checkFiltroAssistidos.setOnCheckedChangeListener((buttonView, isChecked) -> aplicarFiltros());

        editBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void salvarObra() {
        String nome = editNome.getText().toString().trim();
        String anoTexto = editAno.getText().toString().trim();
        String notaTexto = editNota.getText().toString().trim();

        if (nome.isEmpty()) {
            editNome.setError("Digite o nome");
            return;
        }

        if (spinnerTipo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o tipo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerGenero.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o gênero", Toast.LENGTH_SHORT).show();
            return;
        }

        if (anoTexto.isEmpty()) {
            editAno.setError("Digite o ano de lançamento");
            return;
        }

        if (notaTexto.isEmpty()) {
            editNota.setError("Digite a nota pessoal");
            return;
        }

        int anoLancamento;
        double notaPessoal;

        try {
            anoLancamento = Integer.parseInt(anoTexto);
            notaPessoal = Double.parseDouble(notaTexto);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ano ou nota inválidos", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean filmeOuSerie = spinnerTipo.getSelectedItemPosition() == 2; // false = Filme, true = Série
        boolean seJaViuOuN = checkAssistiu.isChecked();
        String genero = spinnerGenero.getSelectedItem().toString();

        if (idSelecionado == null) {
            cadastrarObra(nome, filmeOuSerie, genero, anoLancamento, notaPessoal, seJaViuOuN);
        } else {
            atualizarObra(nome, filmeOuSerie, genero, anoLancamento, notaPessoal, seJaViuOuN);
        }
    }

    private void cadastrarObra(String nome, boolean filmeOuSerie, String genero, int anoLancamento, double notaPessoal, boolean seJaViuOuN) {
        DocumentReference ref = db.collection("obras").document();

        Map<String, Object> dados = montarDados(ref.getId(), nome, filmeOuSerie, genero, anoLancamento, notaPessoal, seJaViuOuN);

        ref.set(dados)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Cadastro realizado", Toast.LENGTH_SHORT).show();
                    limparCampos();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao cadastrar", Toast.LENGTH_SHORT).show());
    }

    private void atualizarObra(String nome, boolean filmeOuSerie, String genero, int anoLancamento, double notaPessoal, boolean seJaViuOuN) {
        Map<String, Object> dados = montarDados(idSelecionado, nome, filmeOuSerie, genero, anoLancamento, notaPessoal, seJaViuOuN);

        db.collection("obras").document(idSelecionado).set(dados)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Registro atualizado", Toast.LENGTH_SHORT).show();
                    limparCampos();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show());
    }

    private Map<String, Object> montarDados(String id, String nome, boolean filmeOuSerie, String genero, int anoLancamento, double notaPessoal, boolean seJaViuOuN) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("id", id);
        dados.put("nome", nome);
        dados.put("FilmeOuSerie", filmeOuSerie);
        dados.put("genero", genero);
        dados.put("anoLancamento", anoLancamento);
        dados.put("notaPessoal", notaPessoal);
        dados.put("seJaViuOuN", seJaViuOuN);
        return dados;
    }

    private void listarObras() {
        db.collection("obras").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Erro ao consultar Firebase", Toast.LENGTH_SHORT).show();
                return;
            }

            listaObras.clear();

            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Obra obra = criarObraDoDocumento(doc);
                    listaObras.add(obra);
                }
            }

            aplicarFiltros();
        });
    }

    private Obra criarObraDoDocumento(QueryDocumentSnapshot doc) {
        String id = doc.getId();
        String nome = doc.getString("nome");
        String genero = doc.getString("genero");
        Boolean filmeOuSerie = doc.getBoolean("FilmeOuSerie");
        Boolean seJaViuOuN = doc.getBoolean("seJaViuOuN");

        Long anoLong = doc.getLong("anoLancamento");
        Number notaNumero = (Number) doc.get("notaPessoal");

        if (nome == null) nome = "";
        if (genero == null) genero = "";
        if (filmeOuSerie == null) filmeOuSerie = false;
        if (seJaViuOuN == null) seJaViuOuN = false;

        int anoLancamento = anoLong == null ? 0 : anoLong.intValue();
        double notaPessoal = notaNumero == null ? 0 : notaNumero.doubleValue();

        return new Obra(id, nome, filmeOuSerie, genero, anoLancamento, notaPessoal, seJaViuOuN);
    }

    private void aplicarFiltros() {
        if (adapter == null || spinnerFiltroTipo.getSelectedItem() == null) {
            return;
        }

        String filtroTipo = spinnerFiltroTipo.getSelectedItem().toString();
        String busca = editBusca.getText().toString().trim().toLowerCase();
        boolean filtrarAssistidos = checkFiltroAssistidos.isChecked();

        listaFiltrada.clear();

        for (Obra obra : listaObras) {
            boolean passaTipo = filtroTipo.equals("Todos os tipos") || obra.getTipoTexto().equals(filtroTipo);
            boolean passaBusca = obra.getNome().toLowerCase().contains(busca);
            boolean passaAssistido = !filtrarAssistidos || obra.isSeJaViuOuN();

            if (passaTipo && passaBusca && passaAssistido) {
                listaFiltrada.add(obra);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void carregarParaEdicao(Obra obra) {
        idSelecionado = obra.getId();

        editNome.setText(obra.getNome());
        editAno.setText(String.valueOf(obra.getAnoLancamento()));
        editNota.setText(String.valueOf(obra.getNotaPessoal()));
        checkAssistiu.setChecked(obra.isSeJaViuOuN());

        spinnerTipo.setSelection(obra.isFilmeOuSerie() ? 2 : 1);
        selecionarSpinner(spinnerGenero, generos, obra.getGenero());

        btnSalvar.setText("Atualizar");
        Toast.makeText(this, "Registro carregado para edição", Toast.LENGTH_SHORT).show();
    }

    private void confirmarExclusao(Obra obra) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir registro")
                .setMessage("Deseja excluir " + obra.getNome() + "?")
                .setPositiveButton("Sim", (dialog, which) -> excluirObra(obra))
                .setNegativeButton("Não", null)
                .show();
    }

    private void excluirObra(Obra obra) {
        db.collection("obras").document(obra.getId()).delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Registro excluído", Toast.LENGTH_SHORT).show();
                    limparCampos();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao excluir", Toast.LENGTH_SHORT).show());
    }

    private void selecionarSpinner(Spinner spinner, String[] opcoes, String valor) {
        for (int i = 0; i < opcoes.length; i++) {
            if (opcoes[i].equals(valor)) {
                spinner.setSelection(i);
                return;
            }
        }
        spinner.setSelection(0);
    }

    private void limparCampos() {
        idSelecionado = null;
        editNome.setText("");
        editAno.setText("");
        editNota.setText("");
        checkAssistiu.setChecked(false);
        spinnerTipo.setSelection(0);
        spinnerGenero.setSelection(0);
        btnSalvar.setText("Salvar");
    }
}
