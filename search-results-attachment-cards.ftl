<#--
	Search Results widget template: Person cards showing the photo, name and age.

	Values come from the "custom_" fields indexed by the
	object-entry-flat-fields module.
-->

<#assign people = searchResultsPortletDisplayContext.getDocuments() />

<#if people?has_content>
	<div class="row">
		<#list people as person>
			<#assign name = person.get("custom_name")!"" />
			<#assign age = person.get("custom_age")!"" />
			<#assign photoId = person.get("custom_photo_id")!"" />

			<#if name?has_content || age?has_content || photoId?has_content>
				<div class="c-mb-4 col-lg-3 col-md-4 col-sm-6">
					<div class="card h-100">
						<#if photoId?matches("[0-9]+")>
							<div class="aspect-ratio aspect-ratio-4-to-3">
								<img
									alt="${htmlUtil.escapeAttribute(name)}"
									class="aspect-ratio-item aspect-ratio-item-center-middle aspect-ratio-item-vertical-fluid"
									src="${themeDisplay.getPathMain()}/document_library/get_file?fileEntryId=${photoId}"
								/>
							</div>
						</#if>

						<div class="card-body">
							<#if name?has_content>
								<h3 class="card-title">${htmlUtil.escape(name)}</h3>
							</#if>

							<#if age?has_content>
								<p class="card-text">Age: ${htmlUtil.escape(age)}</p>
							</#if>
						</div>
					</div>
				</div>
			</#if>
		</#list>
	</div>
</#if>
