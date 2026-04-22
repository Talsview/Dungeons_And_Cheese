# 📚 Documentação Técnica — DungeonsAndCheese

## 📌 Visão Geral
Este documento detalha todas as alterações realizadas no projeto, explicando o que foi implementado, como funciona e por que foi feito.

---

## 🧠 1. GameState (Gerenciamento de Estado)

### O que foi feito
Criação de uma classe central para controlar:
- Fase (floor)
- Score
- Moedas

### Por que foi feito
Evitar espalhar variáveis pelo código e centralizar o estado do jogo.

### Benefícios
- Organização
- Facilidade de manutenção
- Base para upgrades futuros

---

## ⚙️ 2. Otimização de Texturas

### Problema
Cada objeto criava sua própria Texture → alto consumo de memória.

### Solução
Uso de `static` nas texturas.

### Resultado
- Texturas carregadas apenas uma vez
- Melhor performance

---

## 🗺️ 3. Sistema de Salas (RoomType)

### O que foi feito
Criação de enum:
- NORMAL
- SAFE
- TREASURE
- BOSS

### Implementação
- `RoomType` adicionado na classe `Room`
- Método `setRoomType()` controla comportamento

### Impacto
- Gameplay mais variado
- Diferentes funções de sala

---

## 💰 4. Sistema de Recompensas

### O que foi feito
- Sala TREASURE com recompensa
- Coleta ao colidir com hatch

### Código chave
```
if (roomType == RoomType.TREASURE && !rewardCollected)
```

### Benefício
- Incentivo à exploração

---

## 🔫 5. Sistema de Combate

### Melhorias
- Bala colide com parede
- Bala removida corretamente
- Inimigos dão recompensa ao morrer

### Impacto
- Combate mais consistente
- Menos bugs

---

## ❤️ 6. HUD

### Adições
- Barra de vida
- Score
- Moedas
- Tipo da sala

### Problema resolvido
Fonte não suportava caracteres especiais

### Solução
Uso de caracteres:
- #
- =
- -

---

## 🎮 7. Animação da Vida

### Problema
Barra mudava instantaneamente

### Solução
Variável intermediária:
```
displayedPlayerHealth
```

### Resultado
- Animação suave

---

## 🧹 8. Gerenciamento de Recursos

### O que foi feito
- `dispose()` no Player
- `dispose()` seguro no GameScreen

### Bug corrigido
Crash ao morrer causado por:
```
dispose() durante render
```

### Solução
Removido dispose no update

---

## 🧠 9. Estrutura do Código

### Separação de responsabilidades

| Classe       | Responsabilidade |
|-------------|-----------------|
| Room        | Lógica da sala |
| GameState   | Estado global |
| HUD         | Interface |
| GameScreen  | Fluxo principal |

---

## 🏁 Conclusão

O projeto evoluiu significativamente:

✔ Melhor performance  
✔ Código mais limpo  
✔ Gameplay mais variado  
✔ Interface mais informativa  

---

## 🚀 Próximos Passos

- Sistema de loja
- Upgrades
- Novas armas
- IA avançada
- Efeitos visuais

---
