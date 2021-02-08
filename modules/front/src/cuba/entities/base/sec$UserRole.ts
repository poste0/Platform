import { StandardEntity } from "./sys$StandardEntity";
import { User } from "./sec$User";
import { Role } from "./sec$Role";
export class UserRole extends StandardEntity {
  static NAME = "sec$UserRole";
  user?: User | null;
  role?: Role | null;
}
export type UserRoleViewName =
  | "_base"
  | "_local"
  | "_minimal"
  | "tmp.user.edit"
  | "user.edit";
export type UserRoleView<V extends UserRoleViewName> = V extends "tmp.user.edit"
  ? Pick<UserRole, "id" | "user" | "role">
  : V extends "user.edit"
  ? Pick<UserRole, "id" | "role">
  : never;
