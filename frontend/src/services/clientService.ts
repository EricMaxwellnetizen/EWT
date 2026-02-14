import { get, post, put, del, download } from '../lib/api';
import { ENDPOINTS } from '../config/constants';
import type { Client, ClientFormData } from '../types';

// Hierarchy types for nested client data
export interface ClientHierarchy {
  id: number;
  name: string;
  email: string;
  phoneNumber: string;
  address: string;
  createdAt: string;
  projects: ProjectHierarchy[];
}

export interface ProjectHierarchy {
  id: number;
  name: string;
  description: string;
  deadline: string | null;
  isApproved: boolean;
  manager: UserBasic | null;
  progress: number | null;
  epics: EpicHierarchy[];
}

export interface EpicHierarchy {
  id: number;
  name: string;
  description: string;
  dueDate: string | null;
  isApproved: boolean;
  manager: UserBasic | null;
  progress: number | null;
  stories: StoryHierarchy[];
}

export interface StoryHierarchy {
  id: number;
  name: string;
  description: string;
  status: string;
  dueDate: string | null;
  isApproved: boolean;
  assignedTo: UserBasic | null;
  estimatedHours: number | null;
  actualHours: number | null;
}

export interface UserBasic {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
}

/**
 * Normalize client response: backend may send "clientId", frontend expects "id"
 * @param rawClientData - Raw client data from API
 * @returns Normalized client object
 */
const normalizeClient = (rawClientData: any): Client => ({ 
  ...rawClientData, 
  id: rawClientData.id ?? rawClientData.clientId 
});

export const clientService = {
  /**
   * Fetch all clients from the server
   * @returns Promise with array of normalized client objects
   */
  getAll: async (): Promise<Client[]> => {
    const clientsArray = await get<any[]>(ENDPOINTS.CLIENTS);
    return clientsArray.map(normalizeClient);
  },

  /**
   * Fetch a single client by ID
   * @param id - Client ID
   * @returns Promise with normalized client object
   */
  getById: async (id: number): Promise<Client> => {
    const clientData = await get<any>(ENDPOINTS.CLIENT_BY_ID(id));
    return normalizeClient(clientData);
  },
  
  /**
   * Get client with full hierarchy (projects > epics > stories)
   * @param id - Client ID
   * @returns Promise with client hierarchy object
   */
  getHierarchy: (id: number): Promise<ClientHierarchy> => 
    get(`${ENDPOINTS.CLIENT_BY_ID(id)}/hierarchy`),

  /**
   * Create a new client
   * @param clientFormData - Client creation data
   * @returns Promise with created client object
   */
  create: async (clientFormData: ClientFormData): Promise<Client> => {
    const createdClient = await post<any>(ENDPOINTS.CLIENT_CREATE, clientFormData);
    return normalizeClient(createdClient);
  },

  /**
   * Update an existing client
   * @param id - Client ID
   * @param clientUpdateData - Partial client data to update
   * @returns Promise with updated client object
   */
  update: async (id: number, clientUpdateData: Partial<ClientFormData>): Promise<Client> => {
    const updatedClient = await put<any>(ENDPOINTS.CLIENT_UPDATE(id), clientUpdateData);
    return normalizeClient(updatedClient);
  },

  /**
   * Delete a client
   * @param id - Client ID
   * @returns Promise that resolves when delete is complete
   */
  delete: (id: number): Promise<void> => 
    del(ENDPOINTS.CLIENT_DELETE(id)),

  /**
   * Download clients report as Word document
   * @returns Promise that resolves when download starts
   */
  downloadDocument: (): Promise<void> => 
    download(ENDPOINTS.CLIENT_DOWNLOAD, 'clients.docx'),
};
