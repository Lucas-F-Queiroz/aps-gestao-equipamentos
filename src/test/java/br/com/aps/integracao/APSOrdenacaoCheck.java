package br.com.aps.integracao;

import br.com.aps.controller.EquipamentoController;
import br.com.aps.dao.EquipamentoDAO;
import br.com.aps.model.Equipamento;
import br.com.aps.util.DatabaseMigrator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Confere os tres criterios reais de ORDER BY disponibilizados ao usuario.
 * Gera patrimonio unico e remove somente os equipamentos deste teste.
 */
public final class APSOrdenacaoCheck {
    private APSOrdenacaoCheck() {
    }

    public static void main(String[] args) throws Exception {
        DatabaseMigrator.migrate();
        String tag = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        EquipamentoDAO dao = new EquipamentoDAO();
        EquipamentoController controller = new EquipamentoController(dao);
        List<Equipamento> criados = new ArrayList<>();

        try {
            // As tres ordenacoes intencionalmente produzem sequencias diferentes.
            Equipamento zeta = novo("P01-" + tag, "Zeta-" + tag, "B", dao, criados);
            Equipamento alfa = novo("P03-" + tag, "Alfa-" + tag, "C", dao, criados);
            Equipamento meio = novo("P02-" + tag, "Meio-" + tag, "A", dao, criados);

            Set<Integer> ids = criados.stream().map(Equipamento::getId).collect(Collectors.toSet());

            conferir("Nome e patrimonio", controller.listar(), ids,
                List.of(alfa.getId(), meio.getId(), zeta.getId()));
            conferir("Patrimonio e ID", controller.listarPorPatrimonio(), ids,
                List.of(zeta.getId(), meio.getId(), alfa.getId()));
            conferir("Categoria e nome", controller.listarPorCategoria(), ids,
                List.of(meio.getId(), zeta.getId(), alfa.getId()));

            System.out.println("APSOrdenacaoCheck: tres criterios confirmados.");
        } finally {
            for (Equipamento equipamento : criados) {
                dao.excluir(equipamento.getId());
            }
        }
    }

    private static Equipamento novo(String patrimonio, String nome, String categoria,
                                    EquipamentoDAO dao, List<Equipamento> criados) throws Exception {
        Equipamento equipamento = new Equipamento(patrimonio, nome, categoria, Equipamento.ESTADO_BOM);
        dao.inserir(equipamento);
        criados.add(equipamento);
        return equipamento;
    }

    private static void conferir(String criterio, List<Equipamento> lista, Set<Integer> ids,
                                 List<Integer> esperado) {
        List<Integer> resultado = lista.stream()
            .map(Equipamento::getId)
            .filter(ids::contains)
            .toList();
        if (!esperado.equals(resultado)) {
            throw new AssertionError(criterio + ": esperado " + esperado + ", recebido " + resultado);
        }
    }
}
