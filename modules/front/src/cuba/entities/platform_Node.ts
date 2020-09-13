import { StandardEntity } from "./base/sys$StandardEntity";
import { User } from "./base/sec$User";
export class Node extends StandardEntity {
  static NAME = "platform_Node";
  name?: string | null;
  address?: string | null;
  gpu?: string | null;
  cpu?: string | null;
  user?: User | null;
}
export type NodeViewName = "_minimal" | "_local" | "_base";
export type NodeView<V extends NodeViewName> = V extends "_local"
  ? Pick<Node, "id" | "name" | "address" | "gpu" | "cpu">
  : V extends "_base"
  ? Pick<Node, "id" | "name" | "address" | "gpu" | "cpu">
  : never;
