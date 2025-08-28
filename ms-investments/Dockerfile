# --- ESTÁGIO 1: BUILDER ---
# Usamos uma imagem que contém o JDK e o GraalVM (compilador nativo) para Quarkus.
# O 'mandrel' é uma distribuição do GraalVM mantida pela Red Hat.
FROM quay.io/quarkus/mandrel-21-jre:latest AS builder

# Copia todo o contexto do projeto multi-módulo para dentro do container.
# Isso é necessário para que o Maven possa resolver as dependências entre os módulos (ex: a common-library).
COPY . /workspaces/
WORKDIR /workspaces/

# Executa o build do Maven para gerar o executável nativo.
# A flag -Pnative ativa o perfil de compilação nativa.
# O resultado será um único arquivo executável, otimizado e sem dependência da JVM.
RUN mvn package -Pnative -Dquarkus.native.container-build=true

# --- ESTÁGIO 2: RUNTIME ---
# Usamos uma imagem base mínima e segura da Red Hat Universal Base Image.
# Ela contém apenas o estritamente necessário para rodar um executável Linux.
FROM registry.access.redhat.com/ubi8/ubi-minimal:latest

WORKDIR /work
# Copia APENAS o executável nativo gerado no estágio anterior.
# O nome do executável é baseado no <artifactId> do pom.xml + "-runner".
# O coringa (*) garante que ele copie o executável correto, não importa qual microsserviço esteja sendo construído.
COPY --from=builder /workspaces/target/*-runner /work/application

# Expõe a porta padrão do Quarkus.
# Lembre-se que você pode mudar a porta via configuração no application.properties de cada serviço.
EXPOSE 8080

# Comando para iniciar a aplicação.
# O executável nativo é autossuficiente.
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]