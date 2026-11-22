/* ==========================================================
   Queerz — motor do jogo (mocado)

   Pontuação: só quem acerta pontua, e quanto mais rápido, mais
   pontos (de 1000 até 500 em 20 segundos). Errou = 0 pontos.

   Como ainda não há back-end, os outros participantes são
   simulados de forma determinística: o mesmo quiz sempre gera as
   mesmas respostas simuladas. As partidas reais ficam salvas no
   localStorage e entram no ranking e nos gráficos.
   ========================================================== */

const Game = (() => {
  const ATTEMPTS_KEY = "queerz:attempts";
  const MAX_POINTS = 1000;
  const TIME_LIMIT_MS = 20000;
  const CROWD_NAMES = [
    "Mariana", "Sofia", "Pedro", "Lucas", "Beatriz",
    "Rafael", "Camila", "Gabriel", "Larissa", "Thiago",
  ];

  // ---------- Aleatório com semente (resultado sempre igual) ----------
  function hashSeed(text) {
    let h = 1779033703 ^ text.length;
    for (let i = 0; i < text.length; i += 1) {
      h = Math.imul(h ^ text.charCodeAt(i), 3432918353);
      h = (h << 13) | (h >>> 19);
    }
    h = Math.imul(h ^ (h >>> 16), 2246822507);
    h = Math.imul(h ^ (h >>> 13), 3266489909);
    return (h ^ (h >>> 16)) >>> 0;
  }

  function seeded(text) {
    let a = hashSeed(text);
    return () => {
      a = (a + 0x6d2b79f5) | 0;
      let t = Math.imul(a ^ (a >>> 15), 1 | a);
      t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }

  // ---------- Pontuação ----------
  function pointsFor(correct, timeMs) {
    if (!correct) return 0;
    const t = Math.min(Math.max(timeMs, 0), TIME_LIMIT_MS);
    return Math.round(MAX_POINTS * (1 - 0.5 * (t / TIME_LIMIT_MS)));
  }

  function speedLabel(timeMs) {
    if (timeMs <= 5000) return "Respondido rapidamente 🚀";
    if (timeMs <= 12000) return "Boa resposta! 👏";
    return "Acertou, mas dá pra ser mais rápido ⏱️";
  }

  // ---------- Participantes simulados ----------
  function crowd(quiz) {
    return CROWD_NAMES.map((name) => {
      const profile = seeded(`${quiz.id}|${name}`);
      const skill = 0.4 + 0.5 * profile();
      const pace = 2500 + 7500 * profile();

      const answers = quiz.questions.map((question) => {
        const roll = seeded(`${quiz.id}|${name}|${question.id}`);
        const rights = question.options.filter((option) => option.correct);
        const wrongs = question.options.filter((option) => !option.correct);

        const goesRight = roll() < skill || wrongs.length === 0;
        const pool = goesRight && rights.length ? rights : wrongs.length ? wrongs : question.options;
        const option = pool[Math.floor(roll() * pool.length)];
        const timeMs = Math.round(Math.min(19000, Math.max(1200, pace * (0.6 + 0.8 * roll()))));
        const correct = Boolean(option.correct);

        return { questionId: question.id, optionId: option.id, correct, timeMs, points: pointsFor(correct, timeMs) };
      });

      return { name, answers };
    });
  }

  // ---------- Partidas reais (localStorage) ----------
  function readAttempts() {
    try {
      return JSON.parse(localStorage.getItem(ATTEMPTS_KEY)) || [];
    } catch {
      return [];
    }
  }

  function sameName(a, b) {
    return String(a).trim().toLowerCase() === String(b).trim().toLowerCase();
  }

  function attemptsFor(quizId) {
    return readAttempts().filter((attempt) => attempt.quizId === quizId);
  }

  /** Salva a partida. Quem joga de novo com o mesmo nome substitui a anterior. */
  function saveAttempt(quizId, name, answers) {
    const kept = readAttempts().filter(
      (attempt) => !(attempt.quizId === quizId && sameName(attempt.name, name))
    );
    kept.push({
      id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
      quizId,
      name,
      answers,
      total: answers.reduce((sum, answer) => sum + answer.points, 0),
      correctCount: answers.filter((answer) => answer.correct).length,
      finishedAt: Date.now(),
    });
    localStorage.setItem(ATTEMPTS_KEY, JSON.stringify(kept));
    return attemptsFor(quizId).length;
  }

  /** Participantes: simulados + partidas reais já terminadas. */
  function participantsCount(quiz) {
    return CROWD_NAMES.length + attemptsFor(quiz.id).length;
  }

  /** Todos os outros jogadores (simulados e reais), menos quem tem o nome informado. */
  function otherPlayers(quiz, excludeName = "") {
    const real = attemptsFor(quiz.id)
      .filter((attempt) => !excludeName || !sameName(attempt.name, excludeName))
      .map((attempt) => ({ name: attempt.name, answers: attempt.answers }));
    return [...crowd(quiz), ...real];
  }

  // ---------- Ranking ----------
  function pointsThrough(answers, questionIds) {
    const wanted = new Set(questionIds);
    return answers
      .filter((answer) => wanted.has(answer.questionId))
      .reduce((sum, answer) => sum + answer.points, 0);
  }

  /** Placar após as primeiras `count` perguntas. `me` é opcional. */
  function standings(quiz, count, me) {
    const ids = quiz.questions.slice(0, count).map((question) => question.id);
    const entries = otherPlayers(quiz, me ? me.name : "").map((player) => ({
      name: player.name,
      total: pointsThrough(player.answers, ids),
      isMe: false,
    }));

    if (me) {
      entries.push({ name: me.name, total: pointsThrough(me.answers, ids), isMe: true });
    }

    // sort estável: em empate, quem já estava antes fica na frente
    return entries
      .map((entry, index) => ({ entry, index }))
      .sort((a, b) => b.entry.total - a.entry.total || a.index - b.index)
      .map(({ entry }, index) => ({ ...entry, position: index + 1 }));
  }

  /** Linhas exibidas: o topo e, se necessário, a linha de quem está jogando. */
  function visibleRows(ranked, limit = 4) {
    const top = ranked.slice(0, limit);
    const me = ranked.find((entry) => entry.isMe);
    if (!me || top.includes(me)) return top;
    return [...ranked.slice(0, limit - 1), me];
  }

  /** Quantas pessoas escolheram cada alternativa da pergunta. */
  function distribution(quiz, questionIndex, me) {
    const question = quiz.questions[questionIndex];
    const counts = Object.fromEntries(question.options.map((option) => [option.id, 0]));
    let correct = 0;
    let wrong = 0;

    const tally = (answer) => {
      if (!answer || !(answer.optionId in counts)) return;
      counts[answer.optionId] += 1;
      if (answer.correct) correct += 1;
      else wrong += 1;
    };

    otherPlayers(quiz, me ? me.name : "").forEach((player) => {
      tally(player.answers.find((answer) => answer.questionId === question.id));
    });
    if (me) tally(me.answers.find((answer) => answer.questionId === question.id));

    return { counts, correct, wrong };
  }

  // ---------- Textos ----------
  function medalFor(position) {
    return { 1: "🥇", 2: "🥈", 3: "🥉" }[position] || `#${position}`;
  }

  function finalMessage(correctCount, total) {
    const ratio = total ? correctCount / total : 0;
    if (ratio === 1) return "Perfeito! Você arrasou! 🎉";
    if (ratio >= 0.6) return "Muito bem! Ótima pontuação.";
    if (ratio >= 0.3) return "Bom esforço! Dá pra melhorar.";
    return "Não foi dessa vez. Tente de novo!";
  }

  return {
    MAX_POINTS,
    TIME_LIMIT_MS,
    pointsFor,
    speedLabel,
    crowd,
    attemptsFor,
    saveAttempt,
    participantsCount,
    standings,
    visibleRows,
    distribution,
    medalFor,
    finalMessage,
  };
})();

if (typeof module !== "undefined") module.exports = Game;
