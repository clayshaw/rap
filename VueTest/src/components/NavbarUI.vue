<script setup lang="ts">
import { ref } from 'vue'
import { Search, Menu } from 'lucide-vue-next'

defineProps<{
  onToggleSidebar: () => void
}>()


const searchQuery = ref('')


const handleSearch = (e: Event) => {
  e.preventDefault()
  console.log('Search:', searchQuery.value)
}


</script>

<template>
  <nav class="bg-card border-b sticky top-0 z-40">
    <div class="px-4 lg:px-8">
      <!-- <div class="flex items-center justify-between h-16">
        <div class="flex items-center gap-4">
          <button
            @click="onToggleSidebar"
            class="lg:hidden p-2 hover:bg-accent rounded-md"
            aria-label="Toggle menu"
          >
            <Menu :size="20" />
          </button>

          <form @submit="handleSearch" class="hidden md:block">
            <div class="relative">
              <Search :size="18" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <input
                v-model="searchQuery"
                type="search"
                placeholder="Search..."
                class="pl-10 pr-4 py-2 bg-background border rounded-md focus:outline-none focus:ring-2 focus:ring-primary w-64 lg:w-96"
              />
            </div>
          </form>
        </div>

        <div class="flex items-center gap-2">
          <button
            @click="onToggleSidebar"
            class="hidden lg:block p-2 hover:bg-accent rounded-md"
            aria-label="Toggle sidebar"
          >
            <Menu :size="20" />
          </button>

          

            <transition
              enter-active-class="transition ease-out duration-100"
              enter-from-class="transform opacity-0 scale-95"
              enter-to-class="transform opacity-100 scale-100"
              leave-active-class="transition ease-in duration-75"
              leave-from-class="transform opacity-100 scale-100"
              leave-to-class="transform opacity-0 scale-95"
            >
            </transition>
          </div>
        </div> -->
      </div>

      <div class="md:hidden pb-3">
        <form @submit="handleSearch">
          <div class="relative">
            <Search :size="18" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <input
              v-model="searchQuery"
              type="search"
              placeholder="Search..."
              class="w-full pl-10 pr-4 py-2 bg-background border rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
        </form>
      </div>

  </nav>
</template>
