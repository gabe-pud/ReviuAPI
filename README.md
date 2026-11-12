
# 🚀 Reviu Backend

Reviu é uma aplicação inteligente de **flashcards com repetição espaçada**, que combina **Machine Learning e o algoritmo SM-2** para otimizar e personalizar a experiência de aprendizado.

A aplicação utiliza modelos de Machine Learning hospedados na **Microsoft Azure** para analisar informações e padrões de desempenho dos usuários, permitindo recomendações de revisão mais precisas e adaptadas ao ritmo de aprendizado individual. Como mecanismo de contingência, o algoritmo **SM-2** assume o gerenciamento das revisões caso o serviço de Machine Learning fique indisponível.

Este repositório contém todo o backend da aplicação, desenvolvido em **Java + Spring Boot**, com integração a serviços de IA, gerenciamento de mídia e infraestrutura em nuvem.

---

## 🧠 Principais Recursos

### 🔐 Autenticação e Segurança
- Registro e login com **JWT**
- Verificação de e-mail
- Recuperação e redefinição de senha

### 📚 Gestão de Conteúdo
- CRUD completo de **Baralhos** e **Cartões**
- Criação em massa de cartões
- Sistema de **repetição espaçada (Machine Learning & SRS – Algoritmo SM-2)**
- Cards são compartilháveis entre decks e o progresso de revisão é individual por usuário
- Revisões com notas de 0 a 5
- Cold start para o modelo de aprendizado de máquina possuir dados para realizar as previsões exclusivas de repetições de cards para cada usuário

### 🤖 Recursos com IA
- Geração automática de flashcards enviando um **PDF**
- Integração com **Gemini AI**

### 🎧 Suporte a Mídias
- Upload de imagens e áudios
- Busca automática de áudio de pronúncia via Dictionary.com

### ☁️ Infraestrutura em Nuvem
- Cloudinary e Azure para armazenamento
- Swagger/OpenAPI para documentação interativa

---

## 🛠️ Tecnologias Utilizadas

| Categoria | Tecnologias |
|----------|-------------|
| **Linguagem** | Java 17 |
| **Framework** | Spring Boot 3 |
| **Banco de Dados** | PostgreSQL |
| **Segurança** | Spring Security + JWT |
| **Build** | Maven |
| **Nuvem** | Azure, Cloudinary, Gemini AI |
| **Utilitários** | Lombok, PDFBox, JavaMail Sender |
| **Docs** | SpringDoc Swagger |

---

## 🚀 Como Rodar o Projeto

### ✔ Pré-requisitos
- Java **JDK 17**
- Apache **Maven 3.x**
- Banco **PostgreSQL** rodando

---

### 📥 1. Clone o repositório
```bash
git clone https://github.com/thialms/reviu-backend.git
cd reviu-backend
```

### ⚙ 2. Configure as variáveis de ambiente  
Arquivo: `src/main/resources/application.properties`

#### 🔸 Banco de Dados
```
DB_URL=jdbc:postgresql://localhost:5432/reviu_db
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
```

#### 🔸 Segurança
```
SECRET_KEY_TOKEN=chave_super_segura
```

#### 🔸 Gmail SMTP
```
EMAIL=seu_email@gmail.com
EMAIL_PASSWORD=sua_senha_de_app
```

#### 🔸 Cloudinary
```
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=
```

#### 🔸 Cors
```
CORS_URLS=
```

#### 🔸 Gemini AI
```
GEMINI_API_KEY=sua_chave
```

#### 🔸 ML_API_KEY
```
ML_API_KEY=
```
---

### ▶ 3. Inicie o servidor
```bash
./mvnw spring-boot:run
```
ou
```bash
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```
Servidor iniciado em: **http://localhost:8080**

---

## 💻 Como rodar com .env no VS Code (Recomendado)

Para evitar colocar senhas sensíveis no application.properties e correr o risco de enviá-las para o GitHub, você pode usar um arquivo .env e rodar o projeto diretamente pelo Run and Debug do VS Code.

1. Crie um arquivo .env na raiz do projeto com as suas variáveis (ex: DB_URL=...).

2. Adicione o .env ao seu arquivo .gitignore.

3. Crie ou edite o arquivo .vscode/launch.json adicionando a propriedade "envFile":

```bash
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "ReviuApplication",
            "request": "launch",
            "mainClass": "br.edu.fatecpg.reviu.ReviuApplication",
            "projectName": "reviu",
            "envFile": "${workspaceFolder}/.env"
        }
    ]
}
```
Vá na aba Run and Debug do VS Code, selecione ReviuApplication e clique no play para iniciar o servidor.

---

## 📘 Documentação da API

Acesse o Swagger em:

👉 **http://localhost:8080/swagger-ui/index.html**

---

# 📡 Visão Geral dos Endpoints

### 🔑 Autenticação (`/auth`)
- `POST /login` — Login + JWT  
- `POST /register` — Cadastro  
- `POST /verify` — Verificação de código enviado para o e-mail para ativar a conta do usuário
- `POST /resend-verification` — Reenvio da verificação para o e-mail
- `POST /forgot-password` — Solicita código de redefinição de senha
- `POST /reset-password` — Redefine a senha

### 👤 Usuário (`/user`)
- `PUT /change-password` — Alterar senha 
- `PUT /user/friends/request/{senderId}/accept` — Aceitar pedido de amizade
- `PUT /user/change-password` — Altera a senha do usuário
- `POST /user/friends/request/{friendId}` — Enviar pedido de amizade
- `GET /user/info` — Obtém informações do usuário
- `GET /user/search?username={username}` — Pesquisa usuários por username
- `GET /user/friends` — Lista os amigos do usuário autenticado
- `GET /user/friends/requests` — Lista os pedidos de amizades pendentes do usuário autenticado
- `DELETE /user/friends/{friendId}/unfriend` — Desfazer amizade
- `DELETE /user/friends/request/{senderId}/reject` — Recusar pedido de amizade
- `DELETE /user/friends/request/{receiverId}/cancel` — Cancelar pedido de amizade
- `PUT /user/onboarding` — Atualizar configuração de onboarding

### 🗂 Baralhos (`/decks`)
- `POST /` — Criar baralho  
- `GET /` — Listar baralhos  
- `PUT /{deckId}` — Atualizar nome  
- `DELETE /{deckId}` — Excluir baralho  
- `POST /{deckId}/share-requests/{recipientId}` — Enviar um pedido de compartilhamento do deck
- `GET /share-requests` — Listar pedidos de compartilhamento pendentes
- `PUT /share-requests/{requestId}/accept` — Aceitar um pedido e criar a cópia do deck
- `DELETE /share-requests/{requestId}` — Recusar um pedido de compartilhamento

### 🃏 Cartões (`/decks/{deckId}/cards`)
- `POST /` — Criar cartão  
- `POST /bulk` — Criar vários  
- `POST /generate-from-file` — Criar via PDF  
- `GET /` — Listar  
- `GET /due` — Pendentes de revisão  
- `PUT /{cardId}` — Atualizar  
- `DELETE /{cardId}` — Deletar  
- `POST /{cardId}/review` — Enviar nota (0–5)  
- `POST /upload` — Upload de mídia  

---

# 🏅 Créditos

**Desenvolvido por:**

✨ **Thiago de Almeida**<br>
✨ **Levi Ferreira**<br>
✨ **Gabriel Garcia**<br>
🎨 **André Luiz**

**(Equipe de Front-End)**<br>
**André Luiz**

**(Design e prototipação no Figma)**<br>
**Thiago de Almeida**

**(Machine Learning)**<br>
**Thiago de Almeida**

**(Equipe de Back-End)**<br>
**Thiago de Almeida**<br>
**Levi Ferreira**<br>
**Gabriel Garcia**

---
