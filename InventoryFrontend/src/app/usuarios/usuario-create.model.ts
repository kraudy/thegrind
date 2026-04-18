export interface CreateUsuarioRequest {
  usuario: string;
  password: string;
  activo?: boolean;
  roles?: string[];
}