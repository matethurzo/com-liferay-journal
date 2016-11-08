/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.journal.internal.reference;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalFolder;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.BaseLocalService;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.reference.ReferenceSupplier;
import com.liferay.portal.reference.ReferenceSupplierService;
import com.liferay.portal.reference.ReferenceSuppliers;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

/**
 * @author Mate Thurzo
 */
@Component(immediate = true, service = ReferenceSupplierService.class)
public class JournalArticlereferenceSupplierService
	implements ReferenceSupplierService<JournalArticle> {

	@Override
	public DynamicQuery getDynamicQuery() {
		return _journalArticleLocalService.dynamicQuery();
	}

	@Override
	public Class<JournalArticle> getProcessingClass() {
		return JournalArticle.class;
	}

	@Override
	public ReferenceSuppliers<JournalArticle> getReferenceSuppliers() {
		ReferenceSupplier<JournalArticle, JournalFolder>
			folderReferenceSupplier = (article) -> getFolder(article);

		ReferenceSupplier<JournalArticle, DDMTemplate>
			templateReferenceSupplier =
				(article) ->
					Optional.of(article).
						filter(
							(a) ->
								a.getClassNameId() !=
								PortalUtil.getClassNameId(DDMStructure.class)).
						flatMap(
							(filteredArticle) ->
								getDDMTemplate(filteredArticle));

		ReferenceSupplier<JournalArticle, Layout> layoutReferenceSupplier =
			(article) -> Optional.of(article.getLayout());

		ReferenceSupplier<JournalArticle, DDMStructure>
			structureReferenceSupplier = (article) -> getDDMStructure(article);

		return ReferenceSuppliers.create().
			withOutbound(folderReferenceSupplier).
			withOutbound(templateReferenceSupplier).
			withOutbound(layoutReferenceSupplier).
			withOutbound(structureReferenceSupplier);
	}

	@Override
	public BaseLocalService getLocalService() {
		return _journalArticleLocalService;
	}

	private Optional<JournalFolder> getFolder(JournalArticle article) {
		try {
			return Optional.ofNullable(article.getFolder());
		}
		catch (PortalException pe) {
			return Optional.ofNullable(null);
		}
	}

	private Optional<DDMStructure> getDDMStructure(JournalArticle article) {
		try {
			DDMStructure ddmStructure = _ddmStructureLocalService.getStructure(
				article.getGroupId(),
				PortalUtil.getClassNameId(JournalArticle.class),
				article.getDDMStructureKey(), true);

			return Optional.ofNullable(ddmStructure);
		}
		catch (PortalException pe) {
			return Optional.ofNullable(null);
		}
	}

	private Optional<DDMTemplate> getDDMTemplate(JournalArticle article) {
		try {
			DDMTemplate ddmTemplate = _ddmTemplateLocalService.getTemplate(
				article.getGroupId(),
				PortalUtil.getClassNameId(DDMStructure.class),
				article.getDDMTemplateKey(), true);

			return Optional.ofNullable(ddmTemplate);
		}
		catch (PortalException pe) {
			return Optional.ofNullable(null);
		}
	}

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

	@Reference
	private DDMStructureLocalService _ddmStructureLocalService;

	@Reference
	private DDMTemplateLocalService _ddmTemplateLocalService;

}