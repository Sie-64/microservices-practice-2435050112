import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const sourcePath =
  "/Users/zhouyiming/Documents/work/micro_lesson/lesson/.codex-ppt-build/source-compatible.pptx";
const presentation = await PresentationFile.importPptx(
  await FileBlob.load(sourcePath),
);

const result = await presentation.inspect({
  kind: "slide,textbox,shape,table,image,notes",
  include: "id,slide,name,text,textPreview,bbox,isPlaceholder,notes",
  maxChars: 30000,
});
console.log(result.ndjson);
