# Performance

## Banco

Evitar consultas N+1.

Utilizar Fetch Join quando necessário.

Nunca utilizar EAGER sem justificativa.

---

# Paginação

Toda listagem deve ser paginada.

Nunca retornar milhares de registros.

---

# Índices

Criar índices para:

Foreign Keys

Campos pesquisados

Campos únicos

---

# Cache

Utilizar cache apenas quando houver benefício comprovado.

Nunca cachear dados sensíveis.

---

# Consultas

Selecionar apenas colunas necessárias.

Evitar SELECT *

---

# Transações

Manter transações curtas.

Nunca realizar chamadas HTTP dentro de transações.

---

# Processamento

Operações demoradas devem ser assíncronas.

Utilizar eventos sempre que possível.
