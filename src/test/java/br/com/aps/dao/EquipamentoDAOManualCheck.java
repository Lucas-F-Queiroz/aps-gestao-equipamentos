package br.com.aps.dao;

import br.com.aps.model.Equipamento;
import br.com.aps.util.DatabaseMigrator;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Checagem manual de integracao.
 *
 * Antes de executar, crie o banco com database/create_database.sql e
 * configure src/main/resources/application.properties.
 *
 * Rode o metodo main pela IDE. Esta classe nao e um teste JUnit e nao
 * depende de um banco no build normal do Maven.
 */
public class EquipamentoDAOManualCheck {

    public static void main(String[] args) throws SQLException {
        DatabaseMigrator.migrate();

        EquipamentoDAO dao = new EquipamentoDAO();
        String patrimonio = "APS-TEST-" + UUID.randomUUID().toString().substring(0, 12);
        Equipamento equipamento = new Equipamento(
                patrimonio, "Notebook Teste DAO", "NOTEBOOK", Equipamento.ESTADO_BOM
        );

        try {
            dao.inserir(equipamento);
            verificar(equipamento.getId() > 0, "Insercao nao retornou ID.");

            Equipamento porId = dao.buscarPorId(equipamento.getId());
            verificar(porId != null, "Busca por ID falhou.");
            verificar(patrimonio.equals(porId.getPatrimonio()), "Patrimonio incorreto.");

            Equipamento porPatrimonio = dao.buscarPorPatrimonio(patrimonio);
            verificar(porPatrimonio != null, "Busca por patrimonio falhou.");
            verificar(porPatrimonio.getId() == equipamento.getId(), "ID divergente.");

            verificar(contemId(dao.buscarPorNome("Notebook Teste"), equipamento.getId()),
                    "Busca parcial por nome falhou.");
            verificar(contemId(dao.listar(), equipamento.getId()),
                    "Listagem nao encontrou o equipamento.");
            verificar(contemId(dao.listarOrdenadoPorPatrimonio(), equipamento.getId()),
                    "Listagem por patrimonio falhou.");
            verificar(contemId(dao.listarOrdenadoPorCategoria(), equipamento.getId()),
                    "Listagem por categoria falhou.");
            verificar(contemId(dao.listarPorStatus(Equipamento.STATUS_DISPONIVEL),
                            equipamento.getId()), "Filtro de status falhou.");

            equipamento.setNome("Notebook Atualizado");
            equipamento.setEstado(Equipamento.ESTADO_REGULAR);
            equipamento.setStatus(Equipamento.STATUS_INDISPONIVEL);
            verificar(dao.atualizar(equipamento), "Atualizacao falhou.");

            Equipamento atualizado = dao.buscarPorId(equipamento.getId());
            verificar(atualizado != null, "Equipamento nao encontrado apos UPDATE.");
            verificar("Notebook Atualizado".equals(atualizado.getNome()), "Nome nao atualizado.");
            verificar(Equipamento.ESTADO_REGULAR.equals(atualizado.getEstado()),
                    "Estado nao atualizado.");
            verificar(Equipamento.STATUS_INDISPONIVEL.equals(atualizado.getStatus()),
                    "Status nao atualizado.");
            verificar(contemId(dao.listarPorStatus(Equipamento.STATUS_INDISPONIVEL),
                            equipamento.getId()), "Filtro apos UPDATE falhou.");

            verificar(dao.excluir(equipamento.getId()), "Exclusao falhou.");
            verificar(dao.buscarPorId(equipamento.getId()) == null,
                    "Registro nao foi removido.");

            System.out.println("EquipamentoDAO: verificacoes concluidas com sucesso.");
        } finally {
            if (equipamento.getId() > 0 && dao.buscarPorId(equipamento.getId()) != null) {
                dao.excluir(equipamento.getId());
            }
        }
    }

    private static boolean contemId(List<Equipamento> lista, int id) {
        return lista.stream().anyMatch(e -> e.getId() == id);
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
