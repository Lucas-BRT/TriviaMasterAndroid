# Trivia Master Android

Projeto final da disciplina de Desenvolvimento de Software para Dispositivos Móveis (2026.2),
orientado pelo professor Filipe Fernandes dos Santos Brasil de Matos.

Aplicativo Android de quiz que consome a API do [Open Trivia DB](https://opentdb.com/api_config.php)
para gerar perguntas e desafios dinâmicos.

## Requisitos funcionais

1. Identificar o jogador pelo nome antes da partida.
2. Configurar a partida: categoria, dificuldade e quantidade de questões.
3. Buscar questões de múltipla escolha no Open Trivia DB conforme a configuração.
4. Apresentar as questões uma a uma e informar, após cada resposta, se está correta ou incorreta.
5. Exibir um temporizador por questão (entre 10 e 60 segundos) e avançar automaticamente ao esgotar.
6. Calcular a pontuação e exibir o resumo de desempenho ao final da partida.
7. Armazenar localmente os dados do jogador e os resultados de cada partida.
8. Consultar o histórico de partidas anteriores.
9. Tratar falhas de conexão liberando partidas com questões previamente armazenadas.
10. Exibir mensagens adequadas para erros, sem encerrar o aplicativo de forma inesperada.

## Requisitos técnicos

- Consumo REST (HTTP GET) de `https://opentdb.com/api.php`, com parsing do JSON e decodificação de
  HTML entities, e parâmetros dinâmicos de quantidade, categoria e dificuldade.
- Execução assíncrona: rede e persistência fora da UI thread, com acesso ao banco via Content Provider.
- Tratamento de timeout, indisponibilidade da API e ausência de conexão, e validação de `response_code`.
- Uso dos quatro componentes base do Android: Activities, Content Providers, Services e Broadcast Receivers.

## Equipe

- Lucas Brito
- Arthur Gomes
