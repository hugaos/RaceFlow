# Etapa 1: Build do frontend
FROM node:21 AS build
WORKDIR /app

# Copiar apenas os arquivos necessários para instalar dependências
COPY package.json package-lock.json ./

# Instalar dependências
RUN npm install

# Copiar o restante dos arquivos do projeto
COPY . .

# Construir o projeto (gera os arquivos estáticos em /app/dist)
RUN npm run build

# Etapa 2: Configuração do Nginx para servir os arquivos
FROM nginx:stable-alpine

# Copiar os arquivos estáticos para o diretório padrão do Nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Copiar configuração personalizada do Nginx (opcional)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expor a porta onde o Nginx estará disponível
EXPOSE 80

# Comando para iniciar o Nginx
CMD ["nginx", "-g", "daemon off;"]