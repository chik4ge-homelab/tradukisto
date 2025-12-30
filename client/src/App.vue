<template>
  <div class="app-bg min-h-screen px-4 py-8 sm:px-10 sm:py-10">
    <div class="mx-auto flex w-full max-w-5xl flex-col gap-6 sm:gap-8">
      <header class="flex flex-col gap-4">
        <div class="flex items-center justify-between gap-3">
          <div class="flex flex-wrap items-center gap-3">
            <span class="badge-pill">Vue + Vite</span>
            <span class="badge-pill">daisyUI</span>
            <span class="badge-pill">Bun</span>
          </div>
          <label class="label cursor-pointer gap-2">
            <Sun class="h-4 w-4 text-base-content/70" aria-hidden="true" />
            <input
              v-model="isDark"
              type="checkbox"
              class="toggle toggle-secondary toggle-sm rounded-full"
            />
            <MoonStar class="h-4 w-4 text-base-content/70" aria-hidden="true" />
          </label>
        </div>
        <div>
          <h1 class="text-4xl font-semibold tracking-tight text-base-content sm:text-5xl">
            Plamo Translator
          </h1>
        </div>
      </header>

      <section class="grid gap-6 lg:grid-cols-2">
        <div class="card glass-panel border border-base-200/80 shadow-xl min-w-0">
          <div class="card-body gap-6">
            <div class="flex items-center justify-between gap-3">
              <h2 class="card-title text-2xl">Request</h2>
              <div class="flex items-center gap-3">
                <label class="label cursor-pointer gap-3">
                  <span class="label-text text-sm">Stream</span>
                  <input
                    v-model="useStream"
                    type="checkbox"
                    class="toggle toggle-primary toggle-sm rounded-full"
                  />
                </label>
              </div>
            </div>

            <div class="grid grid-cols-[1fr_auto_1fr] items-end gap-2 sm:gap-4">
              <label class="form-control">
                <span class="label-text">Source language</span>
                <select v-model="sourceLanguage" class="select select-bordered bg-base-100 min-w-0">
                  <option v-for="lang in languages" :key="lang" :value="lang">
                    {{ lang }}
                  </option>
                </select>
              </label>
              <button
                class="btn btn-ghost btn-square"
                type="button"
                aria-label="Swap languages"
                @click="swapLanguages"
              >
                <ArrowLeftRight class="h-4 w-4" aria-hidden="true" />
              </button>
              <label class="form-control">
                <span class="label-text">Target language</span>
                <select v-model="targetLanguage" class="select select-bordered bg-base-100 min-w-0">
                  <option v-for="lang in languages" :key="lang" :value="lang">
                    {{ lang }}
                  </option>
                </select>
              </label>
            </div>

            <label class="form-control">
              <div class="flex items-center justify-between">
                <span class="label-text">Text</span>
                <span class="text-xs text-base-content/60">{{ text.length }} chars</span>
              </div>
              <textarea
                v-model="text"
                ref="textAreaRef"
                class="auto-textarea textarea textarea-bordered min-h-[180px] bg-base-100"
                placeholder="Type the text you want to translate"
                rows="6"
              ></textarea>
            </label>

            <div class="flex flex-wrap items-center gap-3">
              <span v-if="statusMessage" class="text-sm text-base-content/70">
                {{ statusMessage }}
              </span>
            </div>

          </div>
        </div>

        <div class="card glass-panel border border-base-200/80 shadow-xl min-w-0">
          <div class="card-body gap-5">
            <div class="flex items-center justify-between">
              <h2 class="card-title text-2xl">Result</h2>
              <button
                class="btn btn-outline btn-sm"
                type="button"
                :disabled="!translated"
                @click="copyResult"
              >
                Copy
              </button>
            </div>

            <div class="rounded-2xl border border-base-200 bg-base-100 p-4 min-h-[240px] min-w-0">
              <p v-if="translated" class="whitespace-pre-wrap text-base text-base-content break-words">
                {{ translated }}
              </p>
              <div v-else class="text-sm text-base-content/60">
                Translation results will appear here.
              </div>
            </div>

            <div v-if="errorMessage" class="alert alert-error">
              <span class="text-sm">{{ errorMessage }}</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { ArrowLeftRight, MoonStar, Sun } from "lucide-vue-next";

const languages = [
  "English",
  "Japanese",
  "Korean",
  "Chinese",
  "French",
  "German",
  "Spanish",
  "Portuguese",
  "Italian"
];

const sourceLanguage = ref("English");
const targetLanguage = ref("Japanese");
const text = ref("");
const translated = ref("");
const isLoading = ref(false);
const errorMessage = ref("");
const useStream = ref(true);
const baseUrl = (import.meta.env.VITE_API_BASE_URL || "").trim();
const bearerToken = (import.meta.env.VITE_BEARER_TOKEN || "").trim();
const isDark = ref(true);
const textAreaRef = ref(null);
const debounceMs = 500;
let debounceTimer = null;
let inFlightController = null;
let lastPayloadKey = "";

const theme = computed(() => (isDark.value ? "dim" : "nord"));

onMounted(() => {
  const stored = localStorage.getItem("theme");
  const initial = stored === "nord" ? "nord" : "dim";
  isDark.value = initial === "dim";
  document.documentElement.setAttribute("data-theme", theme.value);
  document.body.setAttribute("data-theme", theme.value);
  resizeTextarea();
});

watch(isDark, (value) => {
  const next = value ? "dim" : "nord";
  document.documentElement.setAttribute("data-theme", next);
  document.body.setAttribute("data-theme", next);
  localStorage.setItem("theme", next);
});

watch(text, async () => {
  await nextTick();
  resizeTextarea();
});


const canSubmit = computed(() => text.value.length > 0);
const statusMessage = computed(() => {
  if (isLoading.value && useStream.value) return "Streaming response...";
  if (isLoading.value) return "Waiting for response...";
  return "";
});

const endpoint = computed(() => {
  const root = baseUrl;
  const path = useStream.value ? "/api/v1/translate/stream" : "/api/v1/translate";
  if (!root) return path;
  return `${root.replace(/\/$/, "")}${path}`;
});

const resetForm = () => {
  text.value = "";
  translated.value = "";
  errorMessage.value = "";
};

const resizeTextarea = () => {
  if (!textAreaRef.value) return;
  textAreaRef.value.style.height = "auto";
  textAreaRef.value.style.height = `${textAreaRef.value.scrollHeight}px`;
};

const swapLanguages = () => {
  const current = sourceLanguage.value;
  sourceLanguage.value = targetLanguage.value;
  targetLanguage.value = current;
};

const translate = async () => {
  if (!canSubmit.value || isLoading.value) return;
  isLoading.value = true;
  errorMessage.value = "";
  translated.value = "";

  try {
    if (inFlightController) {
      inFlightController.abort();
    }
    inFlightController = new AbortController();
    const response = await fetch(endpoint.value, {
      method: "POST",
      signal: inFlightController.signal,
      headers: {
        "Content-Type": "application/json",
        Accept: useStream.value ? "text/event-stream" : "application/json",
        ...(bearerToken ? { Authorization: `Bearer ${bearerToken}` } : {})
      },
      body: JSON.stringify({
        sourceLanguage: sourceLanguage.value,
        targetLanguage: targetLanguage.value,
        text: text.value
      })
    });

    if (!response.ok) {
      throw new Error(`API error: ${response.status}`);
    }

    if (!useStream.value) {
      const data = await response.json();
      translated.value = data?.translatedText ?? "";
      return;
    }

    if (!response.body) {
      throw new Error("Streaming response is not supported by this browser.");
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";

    const appendEventData = (eventChunk) => {
      const dataLines = [];
      for (const line of eventChunk.split("\n")) {
        if (!line.startsWith("data:")) continue;
        const content = line.startsWith("data: ") ? line.slice(6) : line.slice(5);
        if (content === "[DONE]") return;
        dataLines.push(content);
      }
      if (dataLines.length) {
        translated.value += dataLines.join("\n");
      }
    };

    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });

      const chunks = buffer.split("\n\n");
      buffer = chunks.pop() ?? "";

      for (const chunk of chunks) {
        appendEventData(chunk);
      }
    }

    if (buffer.trim().length) {
      appendEventData(buffer);
    }
  } catch (error) {
    if (error?.name === "AbortError") {
      return;
    }
    errorMessage.value = error instanceof Error ? error.message : "Unknown error";
  } finally {
    isLoading.value = false;
    inFlightController = null;
  }
};

const copyResult = async () => {
  if (!translated.value) return;
  try {
    await navigator.clipboard.writeText(translated.value);
  } catch {
    errorMessage.value = "Copy failed. Please select the text manually.";
  }
};

const scheduleTranslate = () => {
  if (debounceTimer) {
    clearTimeout(debounceTimer);
  }
  if (!canSubmit.value) {
    translated.value = "";
    errorMessage.value = "";
    return;
  }
  const payloadKey = JSON.stringify({
    sourceLanguage: sourceLanguage.value,
    targetLanguage: targetLanguage.value,
    text: text.value,
    stream: useStream.value
  });
  if (payloadKey === lastPayloadKey) return;
  debounceTimer = setTimeout(() => {
    lastPayloadKey = payloadKey;
    translate();
  }, debounceMs);
};

watch([text, sourceLanguage, targetLanguage, useStream], () => {
  scheduleTranslate();
});
</script>
