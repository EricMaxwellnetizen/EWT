/**
 * Reducers Folder
 * 
 * Note: This project uses Zustand for state management (located in /store).
 * This folder is created for project structure compliance with team standards.
 * 
 * If you need traditional reducers in the future, they should be placed here.
 * 
 * Current state management:
 * - Zustand stores: /store (authStore, themeStore, uiStore, etc.)
 * - React Query: For server state
 * - React Hook Form: For form state
 * 
 * Example reducer structure (if needed):
 * ```typescript
 * export const exampleReducer = (state, action) => {
 *   switch (action.type) {
 *     case 'ACTION_TYPE':
 *       return { ...state, ...action.payload };
 *     default:
 *       return state;
 *   }
 * };
 * ```
 */

export const README = 'This folder exists for compliance. State management uses Zustand in /store folder.';
