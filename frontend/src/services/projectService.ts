import { get, post, put, del, download } from '../lib/api';
import { ENDPOINTS } from '../config/constants';
import type { Project, PaginatedResponse, ProjectFormData } from '../types';

/**
 * Normalize project response: backend may send "projectId", frontend expects "id"
 * @param rawProjectData - Raw project data from API
 * @returns Normalized project object
 */
const normalizeProject = (rawProjectData: any): Project => ({ 
  ...rawProjectData, 
  id: rawProjectData.id ?? rawProjectData.projectId 
});

export const projectService = {
  /**
   * Fetch all projects from the server
   * @returns Promise with array of normalized project objects
   */
  getAll: async (): Promise<Project[]> => {
    const projectsArray = await get<any[]>(ENDPOINTS.PROJECTS);
    return projectsArray.map(normalizeProject);
  },

  /**
   * Fetch paginated projects with optional sorting
   * @param params - Pagination and sorting parameters
   * @returns Promise with paginated response containing normalized projects
   */
  getPaginated: async (params: { 
    page: number; 
    size: number; 
    sortBy?: string; 
    sortDirection?: string 
  }): Promise<PaginatedResponse<Project>> => {
    const paginatedResponse = await get<any>(ENDPOINTS.PROJECTS_PAGINATED, params);
    return { 
      ...paginatedResponse, 
      content: (paginatedResponse.content || []).map(normalizeProject) 
    };
  },

  /**
   * Fetch a single project by ID
   * @param id - Project ID
   * @returns Promise with normalized project object
   */
  getById: async (id: number): Promise<Project> => {
    const projectData = await get<any>(ENDPOINTS.PROJECT_BY_ID(id));
    return normalizeProject(projectData);
  },

  /**
   * Create a new project
   * @param projectFormData - Project creation data
   * @returns Promise with created project object
   */
  create: async (projectFormData: ProjectFormData): Promise<Project> => {
    const createdProject = await post<any>(ENDPOINTS.PROJECT_CREATE, projectFormData);
    return normalizeProject(createdProject);
  },

  /**
   * Update an existing project
   * @param id - Project ID
   * @param projectUpdateData - Partial project data to update
   * @returns Promise with updated project object
   */
  update: async (id: number, projectUpdateData: Partial<ProjectFormData>): Promise<Project> => {
    const updatedProject = await put<any>(ENDPOINTS.PROJECT_UPDATE(id), projectUpdateData);
    return normalizeProject(updatedProject);
  },

  /**
   * Delete a project
   * @param id - Project ID
   * @returns Promise that resolves when delete is complete
   */
  delete: (id: number): Promise<void> => 
    del(ENDPOINTS.PROJECT_DELETE(id)),

  /**
   * Download all projects for a specific manager as Word document
   * @param managerId - Manager's user ID
   * @returns Promise that resolves when download starts
   */
  downloadManagerProjects: (managerId: number): Promise<void> => 
    download(ENDPOINTS.PROJECT_DOWNLOAD(managerId), `manager-${managerId}-projects.docx`),
};
