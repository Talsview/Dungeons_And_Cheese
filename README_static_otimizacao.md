# 🚀 Otimização de Performance com Texturas `static`

## 📌 Objetivo
Melhorar a performance do jogo reduzindo o carregamento repetido de texturas em objetos dinâmicos como balas, inimigos e salas.

---

## 🚨 Problema

Antes da otimização, o jogo carregava texturas **toda vez que um objeto era criado**, por exemplo:

- Cada `Bullet` carregava `"bullet.png"`
- Cada `Enemy` carregava `"enemy.png"`
- Cada `Room` carregava todas as texturas do cenário

### ❌ Impactos negativos:
- Alto consumo de memória
- Queda de FPS
- Possíveis travamentos com muitos objetos na tela

---

## ⚙️ Solução

Utilização do modificador `static` nas texturas para que sejam **compartilhadas entre todos os objetos**.

### 🔁 Antes:
```java
private Texture texture;
```

### ✅ Depois:
```java
private static Texture texture;
```

### Carregamento otimizado:
```java
if (texture == null) {
    texture = new Texture("arquivo.png");
}
```

---

## 🧠 Como Funciona

- `static` faz com que a variável pertença à **classe**, não ao objeto
- A textura é carregada **uma única vez**
- Todos os objetos reutilizam a mesma instância

---

## 📊 Comparação

| Situação        | Objetos criados | Texturas carregadas |
|----------------|----------------|---------------------|
| Antes          | 100 balas      | ❌ 100               |
| Depois         | 100 balas      | ✅ 1                 |

---

## 🗂️ Arquivos Alterados

### ✔ `Bullet.java`
- Textura compartilhada entre todas as balas

### ✔ `Enemy.java`
- Textura compartilhada entre todos os inimigos

### ✔ `Room.java`
- Todas as texturas do cenário convertidas para `static`

---

## ⚠️ Alteração no `dispose()`

### ❌ Antes:
```java
texture.dispose();
```

### ✅ Agora:
```java
public void dispose() {
    // não descarta aqui
}
```

### Motivo:
Como as texturas são compartilhadas (`static`), descartá-las em uma instância pode quebrar o jogo inteiro.

---

## 🚀 Benefícios

- ✔ Redução de uso de memória
- ✔ Melhor desempenho (FPS)
- ✔ Menos risco de travamentos
- ✔ Código mais eficiente
- ✔ Base pronta para expansão (mais inimigos, tiros, efeitos)

---

## 📌 Observação

Essa abordagem é uma forma simplificada de gerenciamento de assets.  
Em projetos maiores, recomenda-se o uso do `AssetManager` do LibGDX.

---

## 🏁 Conclusão

A utilização de texturas `static` foi uma melhoria fundamental para a performance do jogo, eliminando carregamentos redundantes e permitindo maior escalabilidade do sistema.
