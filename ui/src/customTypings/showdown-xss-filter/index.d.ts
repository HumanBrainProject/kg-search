import type { ShowdownExtension } from 'showdown';

declare module 'showdown-xss-filter' {
  export default function xssfilter (): ShowdownExtension[];
}