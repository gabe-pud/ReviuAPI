
# 🚀 Reviu Backend

Reviu é uma aplicação poderosa e inteligente de **flashcards com repetição espaçada**, projetada para otimizar seu aprendizado através do algoritmo SM-2.  
Este repositório contém todo o backend da aplicação, desenvolvido em **Java + Spring Boot**, com suporte a IA, mídia e infraestrutura em nuvem.

---

## 🧠 Principais Recursos

### 🔐 Autenticação e Segurança
- Registro e login com **JWT**
- Verificação de e-mail
- Recuperação e redefinição de senha

### 📚 Gestão de Conteúdo
- CRUD completo de **Baralhos** e **Cartões**
- Criação em massa de cartões
- Sistema de **repetição espaçada (SRS – Algoritmo SM-2)**
- Revisões com notas de 0 a 5

### 🤖 Recursos com IA
- Geração automática de flashcards enviando um **PDF**
- Integração com **Gemini AI**

### 🎧 Suporte a Mídias
- Upload de imagens e áudios
- Busca automática de áudio de pronúncia via Dictionary.com

### ☁️ Infraestrutura em Nuvem
- Cloudinary para armazenamento
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
| **Nuvem** | Cloudinary, Gemini AI |
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

#### 🔸 Gemini AI
```
GEMINI_API_KEY=sua_chave
```

---

### ▶ 3. Inicie o servidor
```bash
./mvnw spring-boot:run
```

Servidor iniciado em: **http://localhost:8080**

---

## 📘 Documentação da API

Acesse o Swagger em:

👉 **http://localhost:8080/swagger-ui/index.html**

---

# 🎨 Repositório do Front-End  
O front-end completo da aplicação está aqui:

👉 **https://github.com/gabe-pud/reviu**

---

# 📡 Visão Geral dos Endpoints

### 🔑 Autenticação (`/auth`)
- `POST /login` — Login + JWT  
- `POST /register` — Cadastro  
- `POST /verify` — Verificação de código  
- `POST /resend-verification` — Reenvio  
- `POST /forgot-password` — Solicita código  
- `POST /reset-password` — Redefine senha  

### 👤 Usuário (`/user`)
- `PUT /change-password` — Alterar senha 

### 🗂 Baralhos (`/decks`)
- `POST /` — Criar baralho  
- `GET /` — Listar baralhos  
- `PUT /{deckId}` — Atualizar nome  
- `DELETE /{deckId}` — Excluir baralho  

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

Desenvolvido por:  
✨ **Thiago de Almeida**  
✨ **Levi Ferreira**  

Com colaboração de:  
🎨 **Gabriel Garcia**  
🎨 **André Luiz** 
(Equipe de Front-End)

---
