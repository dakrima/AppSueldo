import {
  ButtonHTMLAttributes,
  Children,
  cloneElement,
  isValidElement,
  ReactElement,
  ReactNode,
} from "react";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  asChild?: false;
  variant?: "primary" | "secondary" | "ghost" | "danger";
  size?: "sm" | "md" | "lg";
};

type ChildButtonProps = {
  asChild: true;
  children: ReactNode;
  className?: string;
  variant?: "primary" | "secondary" | "ghost" | "danger";
  size?: "sm" | "md" | "lg";
};

const variants = {
  primary: "bg-primary text-white shadow-[var(--shadow-paper)] hover:bg-primary-container focus-visible:outline-primary",
  secondary: "border border-border-soft bg-soft-card text-primary hover:border-border-strong hover:bg-white focus-visible:outline-border-strong",
  ghost: "text-text-secondary hover:bg-muted-surface/70 hover:text-primary focus-visible:outline-border-strong",
  danger: "bg-danger text-white hover:bg-red-800 focus-visible:outline-danger",
};

const sizes = {
  sm: "h-9 px-3 text-sm",
  md: "h-11 px-4 text-sm",
  lg: "h-14 px-6 text-base",
};

function classNames(...values: Array<string | undefined>) {
  return values.filter(Boolean).join(" ");
}

export function Button(props: ButtonProps | ChildButtonProps) {
  const variant = props.variant ?? "primary";
  const size = props.size ?? "md";
  const className = classNames(
    "inline-flex items-center justify-center gap-2 rounded-lg font-semibold transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 disabled:cursor-not-allowed disabled:opacity-60",
    variants[variant],
    sizes[size],
    props.className,
  );

  if (props.asChild) {
    const child = Children.only(props.children);

    if (!isValidElement(child)) {
      return null;
    }

    const typedChild = child as ReactElement<{ className?: string }>;
    return cloneElement(typedChild, {
      className: [className, typedChild.props.className].filter(Boolean).join(" "),
    });
  }

  const buttonProps: ButtonHTMLAttributes<HTMLButtonElement> = { ...props, className };
  delete (buttonProps as Partial<ButtonProps>).asChild;
  delete (buttonProps as Partial<ButtonProps>).variant;
  delete (buttonProps as Partial<ButtonProps>).size;

  return <button {...buttonProps} />;
}
