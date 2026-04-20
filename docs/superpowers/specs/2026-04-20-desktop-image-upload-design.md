# Desktop Image Upload Interaction Design

Date: 2026-04-20

## Summary

This design adds a unified desktop image upload interaction across all desktop upload areas in the app. Users will be able to add an image by:

- clicking to choose a file
- dragging an image into the upload area
- pasting an image into the upload area

The selected image will be compressed and uploaded automatically. Mobile behavior will remain unchanged.

If the current form already has an uploaded image, the system must ask for confirmation before replacing it. The existing image must remain in place unless the new upload completes successfully.

## Scope

This design applies only to desktop upload areas in:

- warehouse page
- profit page
- public zone page

Mobile upload interactions are explicitly out of scope for this change.

## Goals

- Provide a faster desktop upload workflow with click, drag-and-drop, and paste support.
- Keep upload behavior consistent across all desktop upload scenarios.
- Reuse the existing compression and upload pipeline instead of creating a parallel flow.
- Prevent accidental replacement when a form already has an uploaded image.
- Avoid regressions in current mobile behavior and backend upload APIs.

## Non-Goals

- No mobile drag-and-drop or paste support.
- No backend API redesign.
- No changes to image compression rules.
- No asset library, image search, or reuse flow.
- No change to how preview URLs or `imageFileId` are stored in form payloads.

## Current Context

The current desktop upload behavior is implemented separately inside multiple page views. Each page ultimately follows the same core flow:

1. select a file from a hidden file input
2. call `compressImageBeforeUpload(file)`
3. call `uploadImage(...)`
4. write the returned `fileId` into the current form

This means the app already has the core upload pipeline. The missing part is a reusable desktop interaction layer that can accept files from multiple entry points and manage replacement rules consistently.

## Approved Product Decisions

- Only desktop upload areas will support drag-and-drop and paste.
- All desktop upload areas will support click, drag-and-drop, and paste.
- If an existing image is already present, the user must confirm before replacement.
- After a new image is accepted, compression and upload should start automatically.
- If compression or upload fails, the old image must remain unchanged.
- Search and asset reuse are not part of this change.

## Recommended Approach

Use a shared desktop upload component as the interaction boundary for all desktop upload areas.

This component should own:

- click-to-select interaction
- drag enter / drag over / drag leave / drop behavior
- paste handling for image clipboard data
- replacement confirmation when an image already exists
- upload state transitions
- success and failure event emission back to the page

The existing pages should stop managing desktop upload interaction details themselves and instead consume the component's outputs.

This is preferred over copying logic into each page because the feature is interaction-heavy and needs to stay behaviorally identical across multiple upload contexts.

## Architecture

### Existing logic to reuse

The following logic should remain the source of truth:

- image compression: `compressImageBeforeUpload(file)`
- upload API call: `uploadImage(file, scene)`
- returned data contract: `fileId` and preview behavior already used by forms

### New UI boundary

Add a reusable desktop upload component for desktop-only upload surfaces.

The component should receive the current upload state from the parent and emit events when:

- a new upload starts
- a new `fileId` is available
- an upload fails
- replacement is canceled

The parent page remains responsible for:

- passing the current `imageFileId`
- deciding upload `scene`
- binding the new `fileId` into the form model
- deciding whether the current page is using desktop or mobile layout

This keeps business data in the page and interaction complexity in one reusable place.

## Component Responsibilities

The desktop upload component should handle:

- hidden native file input
- visible drop zone UI
- drag hover visual state
- paste listener scoped to the upload area
- replacement confirmation
- compression and upload lifecycle
- upload progress / status text for the current area

The component should not:

- decide page-specific form fields other than the image field
- mutate unrelated form data
- change mobile behavior

## Interaction Flow

### New image when no current image exists

1. User clicks, drops, or pastes an image into the desktop upload area.
2. The component extracts the first valid image file.
3. Compression starts immediately.
4. Upload starts immediately after compression succeeds.
5. On success, the component emits the new `fileId`.
6. The page updates its form state and preview.

### New image when a current image already exists

1. User clicks, drops, or pastes an image into the desktop upload area.
2. The component detects that the form already has an image.
3. A confirmation dialog asks whether the user wants to replace the existing image.
4. If the user cancels, nothing changes.
5. If the user confirms, compression and upload begin automatically.
6. The old image remains active until the new upload finishes successfully.
7. If the upload succeeds, the parent receives the new `fileId` and replaces the old one.
8. If the upload fails, the old image remains unchanged.

## File Acceptance Rules

- Only image files are accepted.
- If drag or paste contains multiple images, only the first image is used.
- If paste contains text or non-image clipboard data, normal text paste behavior should remain unaffected.
- Invalid files should show a clear error message and must not replace the current image.

## Error Handling

The interaction must be resilient and conservative:

- compression failure: keep the old image, show an error
- upload failure: keep the old image, show an error
- user cancels replacement: keep the old image, clear temporary candidate state
- drag a non-image file: reject and show an error
- paste non-image content: ignore for upload behavior

Important rule:

The app must not clear a valid existing `imageFileId` until a new upload has completed successfully.

## Desktop-Only Behavior

This feature must not alter mobile interactions.

Desktop pages should render the enhanced upload area.
Mobile pages should continue using the existing interaction model.

Any shared implementation should allow desktop pages to opt in without forcing mobile views to adopt drag-and-drop or paste listeners.

## UX States

The desktop upload area should support these visible states:

- empty: prompt that the user can click, drag, or paste an image
- drag-hover: highlighted drop target state
- uploading: clear status that compression and upload are in progress
- uploaded: current image exists and can be replaced
- error: recent failure message without losing the previous successful image

## Testing Strategy

### Functional coverage

Desktop warehouse page:

- click upload works
- drag upload works
- paste upload works
- replacement requires confirmation

Desktop profit page:

- click upload works
- drag upload works
- paste upload works
- replacement requires confirmation

Desktop public zone page:

- click upload works
- drag upload works
- paste upload works
- replacement requires confirmation

Mobile pages:

- no behavior change

### Failure coverage

- non-image file is rejected
- drag multiple files uses only the first image
- paste multiple clipboard items uses the first valid image
- compression failure does not clear the old image
- upload failure does not clear the old image
- cancel replacement preserves the old image
- dropping a file does not trigger browser default file-open navigation

### Acceptance criteria

- All desktop upload areas support click, drag-and-drop, and paste.
- New files start compression and upload automatically.
- Existing uploaded images are never overwritten without confirmation.
- Failed replacement attempts preserve the old uploaded image and `imageFileId`.
- Text paste outside the image upload interaction remains unaffected.
- Mobile behavior is unchanged.

## Risks

- Paste handling can accidentally interfere with text input if scoped too broadly.
- Drag-and-drop can trigger browser file-open behavior if default events are not blocked correctly.
- Replacement flow can accidentally clear an existing image if state transitions are handled too early.
- Repeating interaction code across pages would create inconsistent behavior, which is why the shared component approach is recommended.

## Rollout Notes

- This change should be introduced behind the shared desktop upload component, then wired into each desktop page.
- The backend upload endpoint remains unchanged, so rollout risk is concentrated in frontend interaction behavior.
- Regression validation should focus on preserving current successful upload behavior while adding the new entry points.
