# Como contribuir

Contribuições devem ajudar a compreender processos, IPC e sincronização, preservando a simplicidade das demonstrações.

1. Descreva o problema ou objetivo em uma issue, com exemplo reproduzível quando possível.
2. Crie uma branch como `fix/pipe-entrada` ou `docs/roteiro-aula`.
3. Faça alterações pequenas, comentando em português o que ajuda o aluno a entender o conceito.
4. Execute `make test`; para alterações Java, execute também `make java`.
5. Atualize a documentação se a entrada, saída ou comportamento mudar.
6. Abra um pull request com motivação, mudança e validação realizada.

Preserve preparacao/originais e as atribuições de autoria. Não inclua executáveis, .class, segredos ou arquivos pessoais. Uma demonstração de corrida deve ser explicitamente identificada como tal.

Use commits descritivos, por exemplo `fix: proteger leitura de count no consumidor`. Não é obrigatório seguir uma convenção de commits automatizada.

A licença do material fornecido ainda precisa ser confirmada. Não adicione uma licença abrangente sem verificar os direitos dos arquivos incluídos.
