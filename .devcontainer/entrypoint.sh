#!/bin/sh

# Define o caminho para o nosso arquivo marcador dentro do workspace
# Este caminho é compartilhado com a sua máquina, então o marcador persistirá
# mesmo que o container seja recriado.
MARKER_FILE="/workspaces/personalbank-suite/.build_complete"

# Verifica se o arquivo marcador NÃO existe
if [ ! -f "$MARKER_FILE" ]; then
    echo ">>> Primeira inicialização detectada. Executando build inicial do Maven..."

    # Executa o build completo do projeto. A flag -DskipTests acelera o processo.
    mvn clean install -DskipTests

    # Verifica se o build foi bem-sucedido antes de criar o marcador
    if [ $? -eq 0 ]; then
        echo ">>> Build inicial concluído com sucesso. Criando arquivo marcador."
        # Cria o arquivo marcador para que este script não execute o build novamente
        touch "$MARKER_FILE"
    else
        echo ">>> ERRO: O build inicial do Maven falhou. O marcador não será criado."
    fi
else
    echo ">>> Arquivo marcador encontrado. Pulando build inicial."
fi

# Linha final crucial: executa qualquer comando que foi passado para o container.
# No nosso caso, será o 'tail -f /dev/null' do docker-compose.dev.yml.
exec "$@"