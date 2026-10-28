-- Nome informado pelo operador ao registrar a devolucao; nao representa login autenticado.
-- Registros anteriores permanecem NULL (responsavel nao informado).
ALTER TABLE devolucao ADD COLUMN registrado_por VARCHAR(100) NULL;
